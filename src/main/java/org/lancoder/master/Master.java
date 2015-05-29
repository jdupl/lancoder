package org.lancoder.master;

import java.io.File;
import java.util.ArrayList;

import org.lancoder.common.Container;
import org.lancoder.common.FilePathManager;
import org.lancoder.common.Node;
import org.lancoder.common.events.Event;
import org.lancoder.common.events.EventListener;
import org.lancoder.common.job.Job;
import org.lancoder.common.network.MessageSender;
import org.lancoder.common.network.cluster.messages.AuthMessage;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.network.messages.web.ApiJobRequest;
import org.lancoder.common.network.messages.web.ApiResponse;
import org.lancoder.common.status.NodeState;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.TaskReport;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.common.third_parties.FFprobe;
import org.lancoder.common.utils.FileUtils;
import org.lancoder.master.api.node.MasterServer;
import org.lancoder.master.api.web.ApiServer;
import org.lancoder.master.checker.NodeCheckerService;
import org.lancoder.master.dispatcher.DispatcherPool;
import org.lancoder.muxer.MuxerListener;
import org.lancoder.muxer.MuxerPool;

public class Master extends Container implements MuxerListener, JobInitiatorListener, EventListener {

	public static final String ALGORITHM = "SHA-256";

	private MasterConfig config;
	private JobInitiator jobInitiator;
	private MasterServer nodeServer;
	private NodeCheckerService nodeChecker;
	private ApiServer apiServer;
	private DispatcherPool dispatcherPool;
	private MuxerPool muxerPool;
	private NodeManager nodeManager;
	private JobManager jobManager;

	private ArrayList<EventListener> eventListeners = new ArrayList<>();

	private MasterSavedInstance savedInstance;

	public Master(MasterConfig config) {
		this.config = config;
		bootstrap();
	}

	@Override
	protected void bootstrap() {
		loadLastInstance();
		super.bootstrap();
	}

	private void loadLastInstance() {
		this.savedInstance = MasterSavedInstance.load(new File(config.getSavedInstancePath()));
	}

	@Override
	protected void registerServices() {
		super.registerServices();

		filePathManager = new FilePathManager(config);

		nodeManager = new NodeManager(this, config, savedInstance);
		eventListeners.add(nodeManager);

		jobInitiator = new JobInitiator(this, config);
		services.add(jobInitiator);

		nodeServer = new MasterServer(config.getNodeServerPort(), this, nodeManager);
		services.add(nodeServer);

		nodeChecker = new NodeCheckerService(this, nodeManager);
		services.add(nodeChecker);

		apiServer = new ApiServer(this);
		services.add(apiServer);

		dispatcherPool = new DispatcherPool(this);
		services.add(dispatcherPool);

		muxerPool = new MuxerPool(this, filePathManager, getFFmpeg());
		services.add(muxerPool);

		jobManager = new JobManager(this, nodeManager, dispatcherPool, savedInstance);
		eventListeners.add(jobManager);
	}

	@Override
	protected void registerThirdParties() {
		registerThirdParty(new FFmpeg(config));
		registerThirdParty(new FFprobe(config));
	}

	public void shutdown() {
		System.out.printf("Executing master shutdown routine.%n"
				+ "Ctrl+C again for immediate shutdown (not recommended).%n");

		// save config and make sure to reset current tasks
		for (Node n : nodeManager.getNodes()) {
			for (ClientTask task : n.getAllTasks()) {
				task.getProgress().reset();
			}
		}
		config.dump();

		// say goodbye to nodes
		for (Node n : nodeManager.getOnlineNodes()) {
			disconnectNode(n);
		}
		stopServices();
		saveInternalState();
	}

	private void saveInternalState() {
		File file = new File(config.getSavedInstancePath());
		MasterSavedInstance current = new MasterSavedInstance(nodeManager.getNodeHashMap(), jobManager.getJobHashMap());
		MasterSavedInstance.save(file, current);
	}

	public MasterConfig getConfig() {
		return config;
	}

	public NodeManager getNodeManager() {
		return nodeManager;
	}

	public JobManager getJobManager() {
		return jobManager;
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
		jobManager.unassingAll(n);
		nodeManager.removeNode(n);

		MessageSender.send(new AuthMessage(ClusterProtocol.DISCONNECT_ME, n.getUnid()), n.getNodeAddress(),
				n.getNodePort());
		System.out.printf("Disconnected node %s.%n", n.getName());
	}

	public void disconnectNode(String unid) {
		disconnectNode(nodeManager.identifySender(unid));
	}

	public boolean addJob(ApiJobRequest j) {
		boolean success = false;
		if (new File(this.getConfig().getAbsoluteSharedFolder(), j.getInputFile()).exists()) {
			success = true;
			this.jobInitiator.process(j);
		}
		return success;
	}

	public ApiResponse apiDeleteJob(String jobId) {
		ApiResponse response = new ApiResponse(true);
		Job j = jobManager.getJob(jobId);

		if (j == null) {
			response = new ApiResponse(false, String.format("Could not retrieve job %s.", jobId));
		} else if (!jobManager.deleteJob(j)) {
			response = new ApiResponse(false, String.format("Could not delete job %s.", jobId));
		}
		return response;
	}

	/**
	 * Check job parts and start muxing process
	 * 
	 * @param job
	 */
	private void jobEncodingCompleted(Job job) {
		if (!checkJobIntegrity(job)) {
			System.err.printf("Cannot start muxing job %s as some task files are missing !%n", job.getJobName());

			for (ClientTask missingTask : job.getTodoTasks()) {
				System.err.printf("Missing file '%s' for task %d'.%n", missingTask.getTempFile(),
						missingTask.getTaskId());
			}

			job.start(false);
		} else {
			// start muxing (or let pool add to the todo list)
			muxerPool.add(job);
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
	public boolean readStatusReport(StatusReport report) {
		NodeState newNodeState = report.status;
		String nodeUnid = report.getUnid();

		// identify node to get it's instance
		Node sender = nodeManager.identifySender(nodeUnid);
		if (sender == null || sender.getStatus() == NodeState.NOT_CONNECTED || report.getTaskReports() == null) {
			return false;
		}

		readTaskReports(report.getTaskReports());
		// only update if status is changed
		if (sender.getStatus() != newNodeState) {
			sender.setStatus(newNodeState);
		}

		// remove unassigned tasks
		ArrayList<ClientTask> reportTasks = new ArrayList<>();
		for (TaskReport taskReport : report.getTaskReports()) {
			reportTasks.add(taskReport.getTask());
		}
		jobManager.removeInvalidAssigments(sender, reportTasks);
		jobManager.updateNodesWork();

		return true;
	}

	/**
	 * Reads all task reports and launches an update of the task status and progress
	 * 
	 * @param reports
	 *            The reports to read
	 */
	public void readTaskReports(ArrayList<TaskReport> reports) {
		for (TaskReport report : reports) {
			ClientTask reportTaskInstance = report.getTask();

			Node sender = nodeManager.identifySender(report.getUnid());
			ClientTask masterTaskInstance = jobManager.getTask(reportTaskInstance.getJobId(),
					reportTaskInstance.getTaskId());

			if (verifyTaskAssignment(masterTaskInstance, sender)) {
				masterTaskInstance.setProgress(reportTaskInstance.getProgress());
				jobManager.taskUpdated(masterTaskInstance, sender);
			}
		}
	}

	private boolean verifyTaskAssignment(ClientTask task, Node node) {
		if (node == null) {
			return false;
		}
		if (!node.hasTask(task)) {
			System.err.printf("Warning: %s instance not found for node %s.%n", task, node.getName());
		}
		return true;
	}

	public void run() {
		startServices();
	}

	@Override
	public void newJob(Job job) {
		this.jobManager.addJob(job);
	}

	@Override
	public void jobMuxingStarted(Job job) {
		job.muxing();
	}

	@Override
	public void jobMuxingCompleted(Job job) {
		System.out.printf("Job %s finished muxing !\n", job.getJobName());
		job.complete();
	}

	@Override
	public void jobMuxingFailed(Job job) {
		System.err.printf("Muxing failed for job %s\n", job.getJobName());
		job.fail();
	}

	@Override
	public void handle(Event event) {
		switch (event.getCode()) {
		case STATUS_REPORT:
			this.readStatusReport((StatusReport) event.getObject());
			break;
		case JOB_ENCODING_COMPLETED:
			jobEncodingCompleted((Job) event.getObject());
			break;
		case CONFIG_UPDATED:
			this.config.dump();
			break;
		default:
			for (EventListener eventListener : eventListeners) {
				eventListener.handle(event);
			}
			break;
		}
	}

	public void cleanJobs() {
		jobManager.cleanJobs();
	}
}