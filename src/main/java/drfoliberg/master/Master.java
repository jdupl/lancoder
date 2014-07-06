package main.java.drfoliberg.master;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import main.java.drfoliberg.common.FFmpegProber;
import main.java.drfoliberg.common.Node;
import main.java.drfoliberg.common.ServerListener;
import main.java.drfoliberg.common.Service;
import main.java.drfoliberg.common.job.FFmpegPreset;
import main.java.drfoliberg.common.job.Job;
import main.java.drfoliberg.common.job.JobConfig;
import main.java.drfoliberg.common.job.RateControlType;
import main.java.drfoliberg.common.network.ClusterProtocol;
import main.java.drfoliberg.common.network.messages.api.ApiJobRequest;
import main.java.drfoliberg.common.network.messages.api.ApiResponse;
import main.java.drfoliberg.common.network.messages.cluster.ConnectMessage;
import main.java.drfoliberg.common.network.messages.cluster.CrashReport;
import main.java.drfoliberg.common.network.messages.cluster.Message;
import main.java.drfoliberg.common.network.messages.cluster.StatusReport;
import main.java.drfoliberg.common.status.JobState;
import main.java.drfoliberg.common.status.NodeState;
import main.java.drfoliberg.common.status.TaskState;
import main.java.drfoliberg.common.task.audio.AudioCodec;
import main.java.drfoliberg.common.task.audio.AudioEncodingTask;
import main.java.drfoliberg.common.task.video.TaskReport;
import main.java.drfoliberg.common.task.video.VideoEncodingTask;
import main.java.drfoliberg.common.utils.FileUtils;
import main.java.drfoliberg.converter.ConverterListener;
import main.java.drfoliberg.converter.ConverterPool;
import main.java.drfoliberg.master.api.node.MasterHttpNodeServer;
import main.java.drfoliberg.master.api.node.MasterNodeServletListener;
import main.java.drfoliberg.master.api.web.ApiServer;
import main.java.drfoliberg.master.checker.HttpNodeChecker;
import main.java.drfoliberg.master.checker.NodeCheckerListener;
import main.java.drfoliberg.master.dispatcher.DispatcherListener;
import main.java.drfoliberg.master.dispatcher.HttpDispatcher;
import main.java.drfoliberg.muxer.Muxer;
import main.java.drfoliberg.muxer.MuxerListener;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Master implements Runnable, MuxerListener, DispatcherListener, ConverterListener, NodeCheckerListener,
		MasterNodeServletListener, ServerListener {

	public static final String ALGORITHM = "SHA-256";
	Logger logger = LoggerFactory.getLogger(Master.class);

	// private MasterNodeServer nodeServer;
	private MasterHttpNodeServer nodeServer;
	// private NodeChecker nodeChecker;
	private HttpNodeChecker nodeChecker;
	private HashMap<String, Node> nodes;

	private ArrayList<Service> services;
	private ConverterPool convertingPool;

	private MasterConfig config;
	private String configPath;

	public HashMap<String, Job> jobs; // change to private after tests

	private ApiServer apiServer;

	public Master(String configPath) {
		services = new ArrayList<>();
		nodes = new HashMap<String, Node>();
		jobs = new HashMap<String, Job>();
		config = MasterConfig.load(configPath);
		this.configPath = configPath;
		if (config != null) {
			System.err.println("Loaded config from disk !");
		} else {
			// this saves default configuration to disk
			this.config = MasterConfig.generate(configPath);
		}

		convertingPool = new ConverterPool(Runtime.getRuntime().availableProcessors()); // exprimental

		// TODO refactor these to observers/events patterns
		// nodeServer = new MasterNodeServer(this);
		nodeServer = new MasterHttpNodeServer(getConfig().getNodeServerPort(), this, this);
		// nodeChecker = new NodeChecker(this);
		nodeChecker = new HttpNodeChecker(this);
		// api server to serve/get information from users
		apiServer = new ApiServer(this);

		services.add(nodeChecker);
		services.add(nodeServer);
		services.add(apiServer);
	}

	public void shutdown() {
		// TODO say goodbye to nodes
		for (Node n : getOnlineNodes()) {
			disconnectNode(n);
		}

		for (Service s : services) {
			s.stop();
		}
		// save config and make sure current tasks are reset
		for (Node n : getNodes()) {
			if (n.getCurrentTask() != null) {
				n.getCurrentTask().reset();
			}
		}
		config.dump(configPath);
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
			int taskCount = j != null ? j.getCountTaskRemaining() : 0;
			if (taskCount != 0 && (min == null || minTaskCount > taskCount)) {
				min = j;
				minTaskCount = taskCount;
			}
		}

		return min != null ? min.getNextTask() : null;
	}

	private AudioEncodingTask getNextAudioTask() {
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
	 * This should look for available online and free nodes. TODO The node order should be intelligent. Fastest node
	 * should be selected.
	 * 
	 * @return pointer to the node object
	 */
	private Node getBestFreeNode() {
		for (Entry<String, Node> entry : nodes.entrySet()) {
			Node n = entry.getValue();
			if (n.getStatus() == NodeState.FREE) {
				return n;
			}
		}
		return null;
	}

	/**
	 * Checks if any task and nodes are available and dispatch until possible.
	 * 
	 * @return true if any work was dispatched
	 */
	public synchronized boolean updateNodesWork() {
		// TODO loop to send more tasks (not just once)
		AudioEncodingTask audioTask = getNextAudioTask();

		while (audioTask != null && convertingPool.hasFreeConverters()) {
			convertingPool.encode(audioTask, this);
			audioTask = getNextAudioTask();
		}

		Node node = getBestFreeNode();
		if (node == null) {
			System.out.println("MASTER: No available nodes!");
			return false;
		}
		VideoEncodingTask nextTask = getNextVideoTask();
		if (nextTask == null) {
			System.out.println("MASTER: No available work!");
			return false;
		}
		dispatch(nextTask, node);
		config.dump(configPath);
		return true;
	}

	public boolean dispatch(VideoEncodingTask task, Node node) {
		if (task.getState() == TaskState.TASK_TODO) {
			task.setState(TaskState.TASK_COMPUTING);
		}
		HttpDispatcher dispatcher = new HttpDispatcher(node, task, this);
		Thread t = new Thread(dispatcher);
		t.start();
		return true;
	}

	private String getNewUNID(Node n) {
		String result = "";
		System.out.println("MASTER: generating a nuid for node " + n.getName());
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
	 * @return Successfully found and removed the node
	 */
	public boolean disconnectNode(Node n) {
//		try {
			VideoEncodingTask t = n.getCurrentTask();
			if (t != null) {
				t.reset();
			}
			// TODO replace old code
//			Socket s = new Socket(n.getNodeAddress(), n.getNodePort());
//			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
//			out.flush();
//			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
//			out.writeObject(new Message(ClusterProtocol.DISCONNECT_ME));
//			out.flush();
//			Object o = in.readObject();
//			if (o instanceof Message) {
//				Message m = (Message) o;
//				switch (m.getCode()) {
//				case BYE:
//					removeNode(n);
//					s.close();
//					break;
//				default:
//					break;
//				}
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return false;
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
		if (n.getUnid().equals("")) {
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
		if (success) {
			updateNodesWork();
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

	public boolean addJob(Job j) {
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
		for (VideoEncodingTask t : j.getTasks()) {
			if (t.getStatus() == TaskState.TASK_COMPUTING) {
				// Find which node has this task
				for (Node n : getNodes()) {
					if (n.getCurrentTask() == t) {
						updateNodeTask(n, TaskState.TASK_CANCELED);
					}
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

	public Job createJob(JobConfig jobConfig, String jobName, int lengthOfTasks, long lengthOfJob, int frameCount,
			float frameRate) {
		// TODO Move to factories (audio task, video task)
		// (this part needs heavy refactor)
		Job j = new Job(jobConfig, jobName, lengthOfTasks, lengthOfJob, frameCount, frameRate,
				config.getFinalEncodingFolder());

		File absoluteSource = FileUtils.getFile(config.getAbsoluteSharedFolder(), jobConfig.getSourceFile());
		File absoluteOutput = FileUtils.getFile(config.getAbsoluteSharedFolder(), j.getOutputFolder(), "audio");
		j.getAudioTasks().add(
				new AudioEncodingTask(AudioCodec.VORBIS, 2, 44100, 3, RateControlType.CRF, absoluteSource
						.getAbsolutePath(), absoluteOutput.getAbsolutePath()));
		return j;
	}

	public ApiResponse addJob(ApiJobRequest req) {
		System.err.println("Reading job request...");
		boolean success = true;

		if (req == null) {
			return new ApiResponse(false, "Job request is probably missing fields !");
		}

		String relativeSourceFile = req.getInputFile();

		ArrayList<File> inputs = new ArrayList<>();
		File absoluteSourceFile = new File(new File(config.getAbsoluteSharedFolder()), relativeSourceFile);

		if (absoluteSourceFile.isDirectory()) {
			System.err.println("Directory given!");
			Collection<File> given = FileUtils.listFiles(absoluteSourceFile, new String[] { "mkv", "mp4", "avi" },
					false); // TODO include recursive ?
			for (File file : given) {
				inputs.add(file);
			}
		} else if (absoluteSourceFile.isFile()) {
			inputs.add(absoluteSourceFile);
		} else {
			return new ApiResponse(false, String.format(
					"File or directory %s in %s does not exists or is not readable !", relativeSourceFile,
					absoluteSourceFile));
		}

		for (File sourceFile : inputs) {
			boolean jobSuccess = true;
			String relative = new File(config.getAbsoluteSharedFolder()).toURI().relativize(sourceFile.toURI())
					.getPath();
			// Set job's name
			String jobName = req.getName();
			if (inputs.size() > 1) {
				String fileName = FilenameUtils.removeExtension(sourceFile.getName());
				jobName = String.format("%s - %s", req.getName(), fileName);
			}
			// Get meta-data from source file
			long lengthOfJob = (long) (FFmpegProber.getSecondsDuration(sourceFile.getAbsolutePath()) * 1000);
			float frameRate = FFmpegProber.getFrameRate(sourceFile.getAbsolutePath());
			int frameCount = (int) Math.floor((lengthOfJob / 1000 * frameRate));

			FFmpegPreset preset = req.getPreset();
			RateControlType rateControlType = req.getRateControlType();

			// Limit to 1 or 2 passes
			byte passes = req.getPasses() < 0 || req.getPasses() > 2 ? 1 : req.getPasses();

			int lengthOfTasks = 1000 * 60 * 5; // TODO get length of task (maybe in an 'advanced section')

			ArrayList<String> extraArgs = new ArrayList<>(); // TODO get extra encoder args from api request

			JobConfig conf = new JobConfig(relative, rateControlType, req.getRate(), passes, preset, extraArgs);
			Job j = createJob(conf, jobName, lengthOfTasks, lengthOfJob, frameCount, frameRate);

			// Create base folders
			File absoluteOutput = FileUtils.getFile(config.getAbsoluteSharedFolder(), j.getOutputFolder());
			File absolutePartsOutput = FileUtils.getFile(absoluteOutput, j.getPartsFolderName());

			if (absoluteOutput.exists()) {
				try {
					// Attempt to clean
					System.err.printf("Directory is not empty. Attempting to clean %s\n", absoluteOutput.toString());
					FileUtils.cleanDirectory(absoluteOutput);
				} catch (IOException e) {
					jobSuccess = false;
					e.printStackTrace();
				}
			} else {
				absoluteOutput.mkdirs();
			}
			FileUtils.givePerms(absoluteOutput, false);
			absolutePartsOutput.mkdir();
			FileUtils.givePerms(absolutePartsOutput, false);

			if (jobSuccess) {
				jobSuccess = addJob(j);
			}
			if (!jobSuccess) {
				success = false;
			}
		}

		if (success) {
			System.err.println("Job(s) added");
			return new ApiResponse(true, "All jobs were successfully added.");
		}
		System.err.println("Error while adding job");
		return new ApiResponse(false, "Some jobs could not be added.");
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
	 * Set disconnected status to node and cancel node's task
	 * 
	 * @param n
	 *            The node to disconnect
	 * @return
	 */
	@Deprecated
	public synchronized boolean removeNode(Node n) {
		if (n != null) {
			// Cancel node's task status if any
			VideoEncodingTask toCancel = null;
			toCancel = n.getCurrentTask();
			if (toCancel != null) {
				updateNodeTask(n, TaskState.TASK_TODO);
				n.setCurrentTask(null);
			}
			n.setStatus(NodeState.NOT_CONNECTED);
		} else {
			System.err.println("Could not mark node as disconnected as it was not found");
		}
		return false;
	}

	public boolean updateNodeTask(Node n, TaskState updateStatus) {
		VideoEncodingTask task = n.getCurrentTask();
		// TODO clean logic here
		if (task == null) {
			System.err.println("MASTER: no task was found for node " + n.getName());
			return false;
		}

		System.out.println("MASTER: the task " + n.getCurrentTask().getTaskId() + " is now " + updateStatus);
		task.setStatus(updateStatus);
		if (updateStatus == TaskState.TASK_COMPLETED) {
			logger.info(String.format("Node %s completed task %d of job %s", n.getName(), n.getCurrentTask()
					.getTaskId(), n.getCurrentTask().getJobId()));
			n.setCurrentTask(null);
			Job job = this.jobs.get(task.getJobId());
			boolean jobDone = true;

			for (VideoEncodingTask t : job.getTasks()) {
				if (t.getState() != TaskState.TASK_COMPLETED) {
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
			n.setCurrentTask(null);
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
	 * Check if all tasks are on the disk after encoding is done. Resets status of bad tasks.
	 * 
	 * @param j
	 *            The job to check
	 * 
	 * @return true if all files are accessible
	 */
	private boolean checkJobIntegrity(Job job) {
		boolean integrity = true;

		for (VideoEncodingTask task : job.getTasks()) {
			File absoluteTaskFile = FileUtils.getFile(config.getAbsoluteSharedFolder(), task.getOutputFile());
			if (!absoluteTaskFile.exists()) {
				System.err.printf("Cannot start muxing ! Task %d of job %s is not found!\n", task.getTaskId(),
						job.getJobName());
				System.err.printf("BTW I was looking for file '%s'\n", absoluteTaskFile);
				integrity = false;
				task.setStatus(TaskState.TASK_TODO);
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
		if (report.getTaskReport() != null) {
			readTaskReport(report.getTaskReport());
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
	 * Reads a task report and launches an update of the task status and progress
	 * 
	 * @param report
	 *            The report to read
	 */
	@Override
	public void readTaskReport(TaskReport report) {
		float progress = report.getTask().getProgress();
		// find node
		String nodeId = report.getUnid();
		Node sender = identifySender(nodeId);

		if (sender == null) {
			return;
		}
		if (!nodeHasTask(sender, report.getTask())) {
			System.err.printf("MASTER: Bad task update from node.");
		} else if (report.getTask().getStatus() == TaskState.TASK_COMPLETED) {
			updateNodeTask(sender, TaskState.TASK_COMPLETED);
		} else {
			System.out.printf("MASTER: Updating the task %s to %f%% \n", sender.getCurrentTask().getTaskId(), progress);
			sender.getCurrentTask().setTaskStatus(report.getTask().getTaskStatus());
		}
	}

	private boolean nodeHasTask(Node n, VideoEncodingTask t) {
		if (n == null) {
			System.err.println("MASTER: Node is null !");
			return false;
		}
		return n.getCurrentTask().equals(t);
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
		updateNodeTask(node, TaskState.TASK_CANCELED);
	}

	public void run() {
		// start services
		// Thread listenerThread = new Thread(nodeServer);
		// listenerThread.start();
		// Thread nodeCheckerThread = new Thread(nodeChecker);
		// nodeCheckerThread.start();
		// Thread apiThread = new Thread(apiServer);
		// apiThread.start();
		for (Service s : this.services) {
			Thread t = new Thread(s);
			t.start();
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
	public synchronized void taskRefused(VideoEncodingTask t, Node n) {
		System.err.printf("Node %s refused task\n", n.getName());
		t.setStatus(TaskState.TASK_TODO);
	}

	@Override
	public synchronized void taskAccepted(VideoEncodingTask t, Node n) {
		System.err.printf("Node %s accepted task\n", n.getName());
		n.setCurrentTask(t);
		t.setStatus(TaskState.TASK_COMPUTING);
	}

	@Override
	public synchronized void convertionFinished(AudioEncodingTask t) {
		System.out.println("MASTER: Finished encoding an audio task");
		t.setTaskState(TaskState.TASK_COMPLETED);
		updateNodesWork();
	}

	@Override
	public synchronized void convertionStarted(AudioEncodingTask t) {
		System.out.println("MASTER: Starting encoding an audio task");
		t.setTaskState(TaskState.TASK_COMPUTING);
	}

	@Override
	public synchronized void convertionFailed(AudioEncodingTask t) {
		System.out.println("MASTER: Failed encoding an audio task");
		t.setTaskState(TaskState.TASK_TODO); // TODO Add something genius here
	}

	@Override
	public void nodeDisconnected(Node n) {
		if (n != null) {
			System.err.printf("Node %s was disconnected !\n", n.getName());
			// Cancel node's task status if any
			VideoEncodingTask toCancel = null;
			toCancel = n.getCurrentTask();
			if (toCancel != null) {
				updateNodeTask(n, TaskState.TASK_TODO);
				n.setCurrentTask(null);
			}
			n.setStatus(NodeState.NOT_CONNECTED);
		} else {
			System.err.println("Could not mark node as disconnected as it was not found");
		}
	}

	@Override
	public void serverShutdown(Service server) {
		// TODO Auto-generated method stub

	}

	@Override
	public void serverFailure(Exception e, Service server) {
		// TODO Auto-generated method stub

	}

	@Override
	public String readConnectRequest(ConnectMessage cm) {
		Node sender = new Node(cm.address, cm.localPort, cm.name);
		sender.setUnid(cm.getUnid());
		if (addNode(sender)) {
			System.err.println("added node " + sender.getUnid());
			return sender.getUnid();
		}
		return null;
	}

}
