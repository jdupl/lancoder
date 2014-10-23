package org.lancoder.master;

import java.io.File;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.lancoder.common.Container;
import org.lancoder.common.Node;
import org.lancoder.common.RunnableService;
import org.lancoder.common.ServerListener;
import org.lancoder.common.job.Job;
import org.lancoder.common.network.cluster.messages.ConnectMessage;
import org.lancoder.common.network.cluster.messages.CrashReport;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.network.messages.web.ApiJobRequest;
import org.lancoder.common.network.messages.web.ApiResponse;
import org.lancoder.common.status.JobState;
import org.lancoder.common.status.NodeState;
import org.lancoder.common.status.TaskState;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.TaskReport;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.common.third_parties.FFprobe;
import org.lancoder.common.utils.FileUtils;
import org.lancoder.master.api.node.MasterNodeServerListener;
import org.lancoder.master.api.node.MasterObjectServer;
import org.lancoder.master.api.web.ApiServer;
import org.lancoder.master.checker.NodeCheckerListener;
import org.lancoder.master.checker.NodeCheckerService;
import org.lancoder.master.dispatcher.DispatchItem;
import org.lancoder.master.dispatcher.DispatcherListener;
import org.lancoder.master.dispatcher.DispatcherPool;
import org.lancoder.muxer.MuxerListener;
import org.lancoder.muxer.MuxerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Master extends Container implements MuxerListener, DispatcherListener, NodeCheckerListener,
		MasterNodeServerListener, ServerListener, JobInitiatorListener {

	public static final String ALGORITHM = "SHA-256";

	Logger logger = LoggerFactory.getLogger(Master.class);
	private MasterConfig config;
	private HashMap<String, Node> nodes = new HashMap<>();
	private HashMap<String, Job> jobs = new HashMap<>();
	private JobInitiator jobInitiator;
	private MasterObjectServer nodeServer;
	private NodeCheckerService nodeChecker;
	private ApiServer apiServer;
	private DispatcherPool dispatcherPool;
	private MuxerPool muxerPool;

	public Master(MasterConfig config) {
		this.config = config;
		basicRoutine();
		// TODO check ffmpeg and mkvmerge
	}

	@Override
	protected void registerServices() {
		super.registerServices();
		jobInitiator = new JobInitiator(this, config);
		services.add(jobInitiator);
		nodeServer = new MasterObjectServer(this, config.getNodeServerPort());
		services.add(nodeServer);
		nodeChecker = new NodeCheckerService(this);
		services.add(nodeChecker);
		apiServer = new ApiServer(this);
		services.add(apiServer);
		dispatcherPool = new DispatcherPool(this);
		services.add(dispatcherPool);
		muxerPool = new MuxerPool(this, config.getAbsoluteSharedFolder());
		services.add(muxerPool);
	}

	@Override
	protected void registerThirdParties() {
		this.thirdParties.add(new FFmpeg(config));
		this.thirdParties.add(new FFprobe(config));
	}

	public void shutdown() {
		// save config and make sure to reset current tasks
		for (Node n : getNodes()) {
			for (ClientTask task : n.getCurrentTasks()) {
				task.getProgress().reset();
			}
		}
		config.dump();
		// say goodbye to nodes
		for (Node n : getOnlineNodes()) {
			disconnectNode(n);
		}
		stopServices();
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
			System.err.printf("WARNING could not FIND NODE %s\n" + "Size of nodesByUNID: %d\n"
					+ "Size of nodes arraylist:%d\n", nodeId, nodes.size(), nodes.size());
		}
		return n;
	}

	/**
	 * Get a list of nodes currently completely free. Video tasks will use all threads.
	 * 
	 * @return A list of nodes that can accept a video task
	 */
	private synchronized ArrayList<Node> getFreeVideoNodes() {
		ArrayList<Node> nodes = new ArrayList<>();
		for (Node node : this.getNodes()) {
			if (node.getStatus() == NodeState.FREE) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	/**
	 * Get a list of nodes that can encode audio. Audio tasks only need one thread.
	 * 
	 * @return A list of nodes that can accept an audio task
	 */
	private synchronized ArrayList<Node> getFreeAudioNodes() {
		ArrayList<Node> nodes = new ArrayList<>();
		for (Node node : this.getNodes()) {
			if (node.getStatus() != NodeState.WORKING || node.getStatus() != NodeState.FREE) {
				boolean nodeAvailable = true;
				// check if any of the task is a video task
				for (ClientTask task : node.getCurrentTasks()) {
					if (task instanceof ClientVideoTask) {
						nodeAvailable = false;
						break;
					}
				}
				// TODO check for each task the task's thread requirements
				if (nodeAvailable && node.getCurrentTasks().size() < node.getThreadCount()) {
					nodes.add(node);
				}
			}
		}
		return nodes;
	}

	/**
	 * Checks if any task and nodes are available and dispatch until possible. Will only dispatch tasks to nodes that
	 * are capable of encoding with the desired library. Always put audio tasks in priority.
	 */
	public synchronized void updateNodesWork() {
		for (Node freeNode : this.getFreeAudioNodes()) {
			boolean nodeDispatched = false;
			ArrayList<Job> jobList = new ArrayList<>(jobs.values());
			Collections.sort(jobList);
			for (Iterator<Job> itJob = jobList.iterator(); itJob.hasNext() && !nodeDispatched;) {
				Job job = itJob.next();
				ArrayList<ClientAudioTask> tasks = job.getTodoAudioTask();
				for (Iterator<ClientAudioTask> itTask = tasks.iterator(); itTask.hasNext() && !nodeDispatched;) {
					ClientAudioTask clientAudioTask = itTask.next();
					if (freeNode.canHandle(clientAudioTask)) {
						nodeDispatched = true;
						dispatch(clientAudioTask, freeNode);
					}
				}
			}
		}

		for (Node freeNode : this.getFreeVideoNodes()) {
			boolean nodeDispatched = false;
			ArrayList<Job> jobList = new ArrayList<>(jobs.values());
			Collections.sort(jobList);
			for (Iterator<Job> itJob = jobList.iterator(); itJob.hasNext() && !nodeDispatched;) {
				Job job = itJob.next();
				ArrayList<ClientVideoTask> tasks = job.getTodoVideoTask();
				for (Iterator<ClientVideoTask> itTask = tasks.iterator(); itTask.hasNext() && !nodeDispatched;) {
					ClientVideoTask clientVideoTask = itTask.next();
					if (freeNode.canHandle(clientVideoTask)) {
						nodeDispatched = true;
						dispatch(clientVideoTask, freeNode);
					}
				}
			}
		}
		config.dump();
	}

	public void dispatch(ClientTask task, Node node) {
		System.err.println("Trying to dispatch to " + node.getName() + " task " + task.getTaskId() + " from "
				+ task.getJobId());
		if (task.getProgress().getTaskState() == TaskState.TASK_TODO) {
			task.getProgress().start();
		}
		node.setStatus(NodeState.LOCKED);
		task.getProgress().start();
		node.addTask(task);
		dispatcherPool.handle(new DispatchItem(task, node));
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
		// remove node from list
		removeNode(n);
		throw new UnsupportedOperationException();
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

	public boolean addJob(ApiJobRequest j) {
		boolean success = false;
		if (new File(this.getConfig().getAbsoluteSharedFolder(), j.getInputFile()).exists()) {
			success = true;
			this.jobInitiator.process(j);
		}
		return success;
	}

	public boolean addJob(Job j) {
		System.out.println("job " + j.getJobName() + " added");
		if (this.jobs.put(j.getJobId(), j) != null) {
			return false;
		}
		updateNodesWork();
		config.dump();
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
			for (ClientTask task : node.getCurrentTasks()) {
				if (task.getJobId().equals(j.getJobId())) {
					task.getProgress().reset();
					taskUpdated(task, node);
				}
			}
		}
		if (this.jobs.remove(j.getJobId()) == null) {
			return false;
		}
		updateNodesWork();
		config.dump();
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
			for (ClientTask t : n.getCurrentTasks()) {
				t.getProgress().reset();
				n.getCurrentTasks().remove(t);
			}
			n.setStatus(NodeState.NOT_CONNECTED);
		} else {
			System.err.println("Could not mark node as disconnected as it was not found");
		}
	}

	public boolean taskUpdated(ClientTask task, Node n) {
		TaskState updateStatus = task.getProgress().getTaskState();
		switch (updateStatus) {
		case TASK_COMPLETED:
			n.getCurrentTasks().remove(task);
			Job job = this.jobs.get(task.getJobId());
			boolean jobDone = true;
			for (ClientTask t : job.getClientTasks()) {
				if (t.getProgress().getTaskState() != TaskState.TASK_COMPLETED) {
					jobDone = false;
					break;
				}
			}
			if (jobDone) {
				jobEncodingCompleted(job);
			}
			updateNodesWork();
			break;
		case TASK_TODO:
		case TASK_CANCELED:
			task.getProgress().reset();
			n.getCurrentTasks().remove(task);
			updateNodesWork();
			break;
		default:
			break;
		}
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
			// start muxing (or let pool add to the todo list)
			muxerPool.handle(job);
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
		for (ClientVideoTask task : job.getClientVideoTasks()) {
			File absoluteTaskFile = FileUtils.getFile(config.getAbsoluteSharedFolder(), task.getTempFile());
			if (!absoluteTaskFile.exists()) {
				System.err.printf("Cannot start muxing ! Task %d of job %s is not found!\n", task.getTaskId(),
						job.getJobName());
				System.err.printf("BTW I was looking for file '%s'\n", absoluteTaskFile);
				integrity = false;
				task.getProgress().reset();
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
			ClientTask reportTask = report.getTask();
			ClientTask actualTask = null;
			String nodeId = report.getUnid();
			Node sender = identifySender(nodeId);

			if (sender == null || !sender.hasTask(reportTask)) {
				System.err.printf("MASTER: Bad task update from node.");
			} else {
				for (ClientTask t : sender.getCurrentTasks()) {
					if (t.equals(reportTask)) {
						actualTask = t;
					}
				}
				TaskState oldState = actualTask.getProgress().getTaskState();
				actualTask.setProgress(reportTask.getProgress());
				if (!oldState.equals(actualTask.getProgress().getTaskState())) {
					System.out.printf("Updating task id %d from %s to %s\n", reportTask.getTaskId(), oldState,
							actualTask.getProgress().getTaskState());
				}
				taskUpdated(actualTask, sender);
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
	}

	public void run() {
		startServices();
	}

	@Override
	public synchronized void taskRefused(DispatchItem item) {
		ClientTask t = item.getTask();
		Node n = item.getNode();
		System.err.printf("Node %s refused task\n", n.getName());
		t.getProgress().reset();
		if (n.hasTask(t)) {
			n.getCurrentTasks().remove(t);
		}
		updateNodesWork();
	}

	@Override
	public synchronized void taskAccepted(DispatchItem item) {
		ClientTask t = item.getTask();
		Node n = item.getNode();
		System.err.printf("Node %s accepted task %d from %s\n", n.getName(), t.getTaskId(), t.getJobId());
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
	public String connectRequest(ConnectMessage cm, InetAddress detectedIp) {
		String unid = null;
		Node sender = cm.getNode();
		sender.setNodeAddress(detectedIp);
		sender.setUnid(cm.getUnid());
		if (addNode(sender)) {
			System.err.println("added node " + sender.getUnid()); // TODO log
			unid = sender.getUnid();
		}
		return unid;
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

	@Override
	public void started(Job job) {
		job.setJobStatus(JobState.JOB_MUXING);
	}

	@Override
	public void completed(Job job) {
		System.out.printf("Job %s finished muxing !\n", job.getJobName());
		job.setJobStatus(JobState.JOB_COMPLETED);
	}

	@Override
	public void failed(Job job) {
		System.err.printf("Muxing failed for job %s\n", job.getJobName());
		// TODO
	}

	@Override
	public void crash(Exception e) {
		// TODO Auto-generated method stub
		e.printStackTrace();
	}

}
