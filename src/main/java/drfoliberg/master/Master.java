package drfoliberg.master;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import drfoliberg.common.Node;
import drfoliberg.common.RunnableService;
import drfoliberg.common.ServerListener;
import drfoliberg.common.Service;
import drfoliberg.common.job.Job;
import drfoliberg.common.network.Routes;
import drfoliberg.common.network.messages.api.ApiJobRequest;
import drfoliberg.common.network.messages.api.ApiResponse;
import drfoliberg.common.network.messages.cluster.ConnectMessage;
import drfoliberg.common.network.messages.cluster.CrashReport;
import drfoliberg.common.network.messages.cluster.StatusReport;
import drfoliberg.common.status.JobState;
import drfoliberg.common.status.NodeState;
import drfoliberg.common.status.TaskState;
import drfoliberg.common.task.Task;
import drfoliberg.common.task.audio.AudioEncodingTask;
import drfoliberg.common.task.video.TaskReport;
import drfoliberg.common.task.video.VideoEncodingTask;
import drfoliberg.common.utils.FileUtils;
import drfoliberg.master.api.node.MasterHttpNodeServer;
import drfoliberg.master.api.node.MasterNodeServletListener;
import drfoliberg.master.api.web.ApiServer;
import drfoliberg.master.checker.NodeChecker;
import drfoliberg.master.checker.NodeCheckerListener;
import drfoliberg.master.dispatcher.DispatchItem;
import drfoliberg.master.dispatcher.DispatcherListener;
import drfoliberg.master.dispatcher.DispatcherPool;
import drfoliberg.muxer.Muxer;
import drfoliberg.muxer.MuxerListener;

public class Master implements Runnable, MuxerListener, DispatcherListener, NodeCheckerListener,
		MasterNodeServletListener, ServerListener, JobInitiatorListener {

	public static final String ALGORITHM = "SHA-256";

	Logger logger = LoggerFactory.getLogger(Master.class);
	private MasterConfig config;
	private String configPath;

	private HashMap<String, Node> nodes = new HashMap<>();
	private HashMap<String, Job> jobs = new HashMap<>();
	private ArrayList<Service> services = new ArrayList<>();
	private JobInitiator jobInitiator;
	private MasterHttpNodeServer nodeServer;
	private NodeChecker nodeChecker;
	private ApiServer apiServer;
	private DispatcherPool dispatcher;

	public Master(String configPath) {
		this.configPath = configPath;
		config = MasterConfig.load(configPath);

		if (config != null) {
			System.err.println("Loaded config from disk !");
		} else {
			// this saves default configuration to disk
			this.config = MasterConfig.generate(configPath);
		}
		jobInitiator = new JobInitiator(this, config);
		nodeServer = new MasterHttpNodeServer(getConfig().getNodeServerPort(), this, this);
		nodeChecker = new NodeChecker(this);
		// api server to serve/get information from users
		apiServer = new ApiServer(this);
		dispatcher = new DispatcherPool(this);

		services.add(nodeChecker);
		services.add(nodeServer);
		services.add(apiServer);
		services.add(jobInitiator);
		services.add(dispatcher);
	}

	public void shutdown() {
		// save config and make sure current tasks are reset
		for (Node n : getNodes()) {
			for (Task task : n.getCurrentTasks()) {
				task.reset();
			}
		}
		config.dump(configPath);
		// say goodbye to nodes
		for (Node n : getOnlineNodes()) {
			disconnectNode(n);
		}
		for (Service s : services) {
			s.stop();
		}
	}

	public MasterConfig getConfig() {
		return config;
	}

	/**
	 * Returns a node object from a node id
	 * 
	 * @param nodeId
	 *            The node ID to get
	 * @return The node object or null if not found
	 */
	public synchronized Node identifySender(String nodeId) {
		Node n = this.nodes.get(nodeId);
		if (n == null) {
			System.out.printf("WARNING could not FIND NODE %s\n" + "Size of nodesByUNID: %d\n"
					+ "Size of nodes arraylist:%d\n", nodeId, nodes.size(), nodes.size());
		}
		return n;
	}

	/**
	 * Get the task of a job with the least possible remaining tasks.
	 * 
	 * @return The task to dispatch next or null is none available
	 */
	private VideoEncodingTask getNextVideoTask() {
		Job min = null;
		int minTaskCount = Integer.MAX_VALUE;

		for (Job j : this.getJobs()) {
			int taskCount = j != null ? j.getTaskRemainingCount() : 0;
			if (taskCount != 0 && (min == null || minTaskCount > taskCount)) {
				min = j;
				minTaskCount = taskCount;
			}
		}
		return min != null ? min.getNextTask() : null;
	}

	private AudioEncodingTask getNextAudioTask() {
		// We should process all audio in no particular order
		for (Job j : this.getJobs()) {
			for (AudioEncodingTask task : j.getAudioTasks()) {
				if (task.getTaskState() == TaskState.TASK_TODO) {
					return task;
				}
			}
		}
		return null;
	}

	/**
	 * This should look for available online and free nodes.
	 * 
	 * @return pointer to the node object
	 */
	private synchronized Node getBestFreeNode() {
		Node best = null;
		int minTasks = Integer.MAX_VALUE;

		for (Entry<String, Node> entry : nodes.entrySet()) {
			Node n = entry.getValue();
			if (n.getStatus() == NodeState.FREE) {
				if (n.getCurrentTasks().size() < minTasks) {
					best = n;
				}
			}
		}
		return best;
	}

	private synchronized Node getBestAudioNode() {
		// Maximum concurrent audio jobs
		int maxConcurentTasks = 3; // TODO should use node data
		Node best = null;
		int minTasks = Integer.MAX_VALUE;

		for (Entry<String, Node> entry : nodes.entrySet()) {
			Node n = entry.getValue();
			if (n.getCurrentTasks().size() < maxConcurentTasks && n.getCurrentTasks().size() < minTasks) {
				best = n;
			}
		}
		return best;
	}

	/**
	 * Checks if any task and nodes are available and dispatch until possible.
	 * 
	 * @return true if any work was dispatched
	 */
	public void updateNodesWork() {
		System.out.println("MASTER: Checking dispatch");
		Node node = null;
		VideoEncodingTask nextVideoTask = null;
		AudioEncodingTask nextAudioTask = null;

		while (((nextAudioTask = getNextAudioTask()) != null && (node = getBestAudioNode()) != null)) {
			dispatch(nextAudioTask, node);
		}
		// Avoid looping through jobs as it might be cpu intensive in large projects
		// Order of the while condition is important as looping through workers is faster
		while ((node = getBestFreeNode()) != null && (nextVideoTask = getNextVideoTask()) != null) {
			System.err.println("Trying to dispatch to " + node.getName() + " task " + nextVideoTask.getTaskId());
			dispatch(nextVideoTask, node);
		}
		config.dump(configPath);
	}

	public void dispatch(Task task, Node node) {
		if (task.getTaskState() == TaskState.TASK_TODO) {
			task.setTaskState(TaskState.TASK_COMPUTING);
		}
		node.setStatus(NodeState.LOCKED);
		dispatcher.dispatch(new DispatchItem(task, node));
	}

	private String getNewUNID(Node n) {
		String result = "";
		System.out.println("MASTER: generating a unid for node " + n.getName());
		long ms = System.currentTimeMillis();
		String input = ms + n.getName();
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			// print and handle exception
			// if a null string is given back to the client, it won't connect
			e.printStackTrace();
			System.out
					.println("MASTER: could not get an instance of " + ALGORITHM + " to produce a UNID\nThis is bad.");
			return "";
		}
		byte[] byteArray = md.digest(input.getBytes());
		result = "";
		for (int i = 0; i < byteArray.length; i++) {
			result += Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1);
		}
		System.out.println("MASTER: generated " + result + " for node " + n.getName());
		return result;
	}

	/**
	 * Sends a disconnect request to a node, removes the node from the node list and updates the task of the node if it
	 * had any.
	 * 
	 * @param n
	 *            The node to remove
	 */
	public void disconnectNode(Node n) {
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(2000)
					.setConnectionRequestTimeout(2000).setConnectTimeout(2000).build();
			URI url = new URI("http", null, n.getNodeAddress().getHostAddress(), n.getNodePort(),
					Routes.DISCONNECT_NODE, null, null);
			HttpPost post = new HttpPost(url);
			post.setConfig(defaultRequestConfig);
			client.execute(post);
		} catch (IOException e) {
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			// remove node from list
			removeNode(n);
		}
	}

	/**
	 * Adds a node to the node list. Assigns a new ID to the node if it's non-existent. The node will be picked up by
	 * the node checker automatically if work is available.
	 * 
	 * @param n
	 *            The node to be added
	 * @return if the node could be added
	 */
	public boolean addNode(Node n) {
		boolean success = true;
		// Is this a new node ?
		if (n.getUnid() == null || n.getUnid().equals("")) {
			n.setUnid(getNewUNID(n));
		}
		Node masterInstance = nodes.get(n.getUnid());
		if (masterInstance != null && masterInstance.getStatus() == NodeState.NOT_CONNECTED) {
			// Node with same unid reconnecting
			nodes.get(n.getUnid()).setStatus(NodeState.NOT_CONNECTED);
		} else if (masterInstance == null) {
			n.setStatus(NodeState.NOT_CONNECTED);
			nodes.put(n.getUnid(), n);
			System.out.println("MASTER: Added node " + n.getName() + " with unid: " + n.getUnid());
		} else {
			success = false;
		}
		return success;
	}

	public ArrayList<Node> getNodes() {
		ArrayList<Node> nodes = new ArrayList<>();
		for (Entry<String, Node> e : this.nodes.entrySet()) {
			nodes.add(e.getValue());
		}
		return nodes;
	}

	public void addJob(ApiJobRequest j) {
		this.jobInitiator.process(j);
	}

	public boolean addJob(Job j) {
		System.out.println("job " + j.getJobName() + " added");
		if (this.jobs.put(j.getJobId(), j) != null) {
			return false;
		}
		updateNodesWork();
		config.dump(configPath);
		return true;
	}

	public ApiResponse apiDeleteJob(String jobId) {
		ApiResponse response = new ApiResponse(true);
		Job j = this.jobs.get(jobId);
		if (j == null) {
			response = new ApiResponse(false, String.format("Could not retrieve job %s.", jobId));
		} else if (!deleteJob(j)) {
			response = new ApiResponse(false, String.format("Could not delete job %s.", jobId));
		}
		return response;
	}

	public boolean deleteJob(Job j) {
		if (j == null) {
			return false;
		}

		for (Node node : this.getNodes()) {
			for (Task task : node.getCurrentTasks()) {
				if (task.getJobId().equals(j.getJobId())) {
					updateNodeTask(task, node, TaskState.TASK_CANCELED);
				}
			}
		}

		if (this.jobs.remove(j.getJobId()) == null) {
			return false;
		}
		updateNodesWork();
		config.dump(configPath);
		return true;
	}

	public ArrayList<Job> getJobs() {
		ArrayList<Job> jobs = new ArrayList<>();
		for (Entry<String, Job> e : this.jobs.entrySet()) {
			jobs.add(e.getValue());
		}
		return jobs;
	}

	public ArrayList<Node> getOnlineNodes() {
		ArrayList<Node> nodes = new ArrayList<>();
		for (Entry<String, Node> e : this.nodes.entrySet()) {
			Node n = e.getValue();
			if (n.getStatus() != NodeState.PAUSED && n.getStatus() != NodeState.NOT_CONNECTED) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	/**
	 * Set disconnected status to node and cancel node's tasks. Use shutdownNode() to gracefully shutdown a node.
	 * 
	 * 
	 * @param n
	 *            The node to disconnect
	 */
	public synchronized void removeNode(Node n) {
		if (n != null) {
			// Cancel node's tasks status if any
			for (Task t : n.getCurrentTasks()) {
				t.reset();
				n.getCurrentTasks().remove(t);
			}
			n.setStatus(NodeState.NOT_CONNECTED);
		} else {
			System.err.println("Could not mark node as disconnected as it was not found");
		}
	}

	public boolean updateNodeTask(Task task, Node n, TaskState updateStatus) {
		// TODO clean logic here
		if (task == null) {
			System.err.println("MASTER: no task was found for node " + n.getName());
			return false;
		}
		task.setTaskState(updateStatus);
		if (updateStatus == TaskState.TASK_COMPLETED) {
			n.getCurrentTasks().remove(task);
			Job job = this.jobs.get(task.getJobId());
			boolean jobDone = true;
			for (Task t : job.getTasks()) {
				if (t.getTaskState() != TaskState.TASK_COMPLETED) {
					jobDone = false;
					break;
				}
			}
			if (jobDone) {
				jobEncodingCompleted(job);
			}
			// TODO implement task.complete() ?
		} else if (updateStatus == TaskState.TASK_CANCELED) {
			dispatch(task, n);
			n.getCurrentTasks().remove(task);
		}
		updateNodesWork();

		return false;
	}

	/**
	 * Check job parts and start muxing process
	 * 
	 * @param job
	 */
	private void jobEncodingCompleted(Job job) {
		job.setJobStatus(JobState.JOB_ENCODED);
		if (!checkJobIntegrity(job)) {
			job.setJobStatus(JobState.JOB_COMPUTING);
		} else {
			// start muxing
			File absoltuteJobOutputFolder = new File(config.getAbsoluteSharedFolder(), job.getOutputFolder());
			Muxer m = new Muxer(this, job, absoltuteJobOutputFolder.getAbsolutePath());
			// this.services.add(m); TODO add muxer to services
			Thread t = new Thread(m);
			t.start();
		}
	}

	/**
	 * Check if all tasks are on the disk after encoding is done. Resets status of missing tasks.
	 * 
	 * @param job
	 *            The job to check
	 * 
	 * @return true if all files are accessible
	 */
	private boolean checkJobIntegrity(Job job) {
		boolean integrity = true;

		for (VideoEncodingTask task : job.getVideoTasks()) {
			File absoluteTaskFile = FileUtils.getFile(config.getAbsoluteSharedFolder(), task.getOutputFile());
			if (!absoluteTaskFile.exists()) {
				System.err.printf("Cannot start muxing ! Task %d of job %s is not found!\n", task.getTaskId(),
						job.getJobName());
				System.err.printf("BTW I was looking for file '%s'\n", absoluteTaskFile);
				integrity = false;
				task.setTaskState(TaskState.TASK_TODO);
			}
		}
		return integrity;
	}

	/**
	 * Reads a status report of a node and updates the status of the node.
	 * 
	 * @param report
	 *            The report to be read
	 * @return true if update could be sent, false otherwise
	 */
	@Override
	public void readStatusReport(StatusReport report) {
		NodeState s = report.status;
		String unid = report.getUnid();
		Node sender = identifySender(unid);
		if (report.getTaskReports() != null) {
			readTaskReports(report.getTaskReports());
		}
		// only update if status is changed
		if (sender.getStatus() != report.status) {
			System.out.printf("node %s is updating it's status from %s to %s\n", sender.getName(), sender.getStatus(),
					report.status);
			sender.setStatus(s);
			updateNodesWork();
		} else {
			System.out.printf("Node %s is still alive\n", sender.getName());
		}
	}

	/**
	 * Reads all task reports and launches an update of the task status and progress
	 * 
	 * @param reports
	 *            The reports to read
	 */
	@Override
	public void readTaskReports(ArrayList<TaskReport> reports) {
		for (TaskReport report : reports) {
			Task reportTask = report.getTask();
			Task actualTask = null;
			String nodeId = report.getUnid();
			Node sender = identifySender(nodeId);

			if (sender == null || !sender.hasTask(reportTask)) {
				System.err.printf("MASTER: Bad task update from node.");
			} else {
				for (Task t : sender.getCurrentTasks()) {
					if (t.equals(reportTask)) {
						actualTask = t;
					}
				}
				if (reportTask.getTaskState() == TaskState.TASK_COMPLETED) {
					updateNodeTask(actualTask, sender, TaskState.TASK_COMPLETED);
				} else {
					System.out.printf("Updating task id %d from %s to %s\n", reportTask.getTaskId(),
							actualTask.getTaskState(), reportTask.getTaskState());
					actualTask.setTaskState(report.getTask().getTaskState());
					if (reportTask instanceof VideoEncodingTask) {
						VideoEncodingTask vTask = (VideoEncodingTask) reportTask;
						float progress = vTask.getProgress();
						System.out.printf("MASTER: Updating the task %d to %f%% \n", reportTask.getTaskId(), progress);
						vTask.setTaskStatus(vTask.getTaskStatus());
					}
				}
			}
		}
	}

	public void readCrashReport(CrashReport report) {
		// TODO handle non fatal crashes (worker side first)
		// after a non-fatal crash, master should try X times to reassign tasks
		// from same job. After a fatal crash, leave the node connected but do
		// not assign anything to the node.
		// This way, node can reconnected if fatal crash is fixed.
		Node node = identifySender(report.getUnid());
		if (report.getCause().isFatal()) {
			System.err.printf("Node '%s' fatally crashed.\n", node.getName());
		} else {
			System.out.printf("Node %s crashed but not fatally.\n", node.getName());
		}
		// updateNodeTask(node, TaskState.TASK_CANCELED);
	}

	public void run() {
		for (Service s : services) {
			if (s instanceof RunnableService) {
				Thread t = new Thread((RunnableService) s);
				t.start();
			}
		}
	}

	@Override
	public void muxingStarting(Job job) {
		job.setJobStatus(JobState.JOB_MUXING);
	}

	@Override
	public void muxingCompleted(Job job) {
		System.out.printf("Job %s finished muxing !\n", job.getJobName());
		job.setJobStatus(JobState.JOB_COMPLETED);
	}

	@Override
	public void muxingFailed(Job job, Exception e) {
		// TODO Do something more (implement job failure ?)
		System.err.printf("Muxing failed for job %s\n", job.getJobName());
		e.printStackTrace();
	}

	@Override
	public synchronized void taskRefused(DispatchItem item) {
		Task t = item.getTask();
		Node n = item.getNode();
		System.err.printf("Node %s refused task\n", n.getName());
		t.setTaskState(TaskState.TASK_TODO);
		n.setStatus(NodeState.FREE);
		updateNodesWork();
	}

	@Override
	public synchronized void taskAccepted(DispatchItem item) {
		Task t = item.getTask();
		Node n = item.getNode();
		System.err.printf("Node %s accepted task\n", n.getName());
		n.addTask(t);
		t.setTaskState(TaskState.TASK_ASSIGNED);
	}

	@Override
	public void nodeDisconnected(Node n) {
		this.removeNode(n);
	}

	@Override
	public void serverShutdown(RunnableService server) {
		// TODO Auto-generated method stub
	}

	@Override
	public void serverFailure(Exception e, RunnableService server) {
		// TODO Auto-generated method stub
	}

	@Override
	public String connectRequest(ConnectMessage cm) {
		Node sender = new Node(cm.address, cm.localPort, cm.name);
		sender.setUnid(cm.getUnid());
		if (addNode(sender)) {
			System.err.println("added node " + sender.getUnid());
			return sender.getUnid();
		}
		// Could not add node
		return null;
	}

	@Override
	public void disconnectRequest(ConnectMessage cm) {
		Node n = identifySender(cm.getUnid());
		this.removeNode(n);
	}

	@Override
	public void newJob(Job job) {
		this.addJob(job);
	}
}
