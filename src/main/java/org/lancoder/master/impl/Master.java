package org.lancoder.master.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import org.lancoder.common.Container;
import org.lancoder.common.FilePathManager;
import org.lancoder.common.Node;
import org.lancoder.common.config.Config;
import org.lancoder.common.config.ConfigManager;
import org.lancoder.common.events.Event;
import org.lancoder.common.events.EventEnum;
import org.lancoder.common.events.EventListener;
import org.lancoder.common.job.Job;
import org.lancoder.common.logging.LogCollectorHandler;
import org.lancoder.common.network.MessageSender;
import org.lancoder.common.network.cluster.messages.AuthMessage;
import org.lancoder.common.network.cluster.messages.LogRecordMessage;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.network.messages.web.ApiResponse;
import org.lancoder.common.status.NodeState;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.TaskReport;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.common.third_parties.FFprobe;
import org.lancoder.common.third_parties.MkvMerge;
import org.lancoder.common.utils.FileUtils;
import org.lancoder.master.ClusterLogCollector;
import org.lancoder.master.JobInitiator;
import org.lancoder.master.JobManager;
import org.lancoder.master.MasterConfig;
import org.lancoder.master.MasterSavedInstance;
import org.lancoder.master.NodeManager;
import org.lancoder.master.api.node.MasterServer;
import org.lancoder.master.api.web.ApiServer;
import org.lancoder.master.checker.NodeCheckerService;
import org.lancoder.master.dispatcher.DispatcherPool;
import org.lancoder.muxer.MuxerPool;

public class Master extends Container implements EventListener {

	public static final String ALGORITHM = "SHA-256";

	private JobInitiator jobInitiator;
	private MasterServer nodeServer;
	private NodeCheckerService nodeChecker;
	private ApiServer apiServer;
	private DispatcherPool dispatcherPool;
	private MuxerPool muxerPool;
	private NodeManager nodeManager;
	private JobManager jobManager;
	private ConfigManager<MasterConfig> configManager;
	private MasterSavedInstance savedInstance;
	private LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();

	private ArrayList<EventListener> eventListeners = new ArrayList<>();
	private MasterAdapter eventListener;
	private LogCollectorHandler webUIHandler;
	private ClusterLogCollector clusterLogCollector;

	@Override
	public void handle(Event event) {
		this.eventQueue.add(event);
	}

	@Override
	public void setConfigManager(ConfigManager<? extends Config> config) {
		@SuppressWarnings("unchecked")
		ConfigManager<MasterConfig> manager = (ConfigManager<MasterConfig>) config;
		this.configManager = manager;
	}

	@Override
	public Class<? extends Config> getConfigClass() {
		return MasterConfig.class;
	}

	@Override
	public void bootstrap() {
		clusterLogCollector = new ClusterLogCollector();
		webUIHandler = new LogCollectorHandler(clusterLogCollector);
		webUIHandler.setLevel(Level.ALL);
		logger.addHandler(webUIHandler);

		eventListener = new MasterAdapter(this);
		loadLastInstance();
		super.bootstrap();
	}

	private void loadLastInstance() {
		this.savedInstance = MasterSavedInstance.load(new File(getConfig().getSavedInstancePath()));
	}

	@Override
	protected void registerServices() {
		super.registerServices();

		filePathManager = new FilePathManager(getConfig());

		nodeManager = new NodeManager(eventListener, getConfig(), savedInstance);
		eventListeners.add(nodeManager);

		jobInitiator = new JobInitiator(eventListener, getConfig());
		services.add(jobInitiator);

		nodeServer = new MasterServer(getConfig().getNodeServerPort(), eventListener, nodeManager);
		services.add(nodeServer);

		nodeChecker = new NodeCheckerService(eventListener, nodeManager);
		services.add(nodeChecker);

		apiServer = new ApiServer(this);
		services.add(apiServer);

		dispatcherPool = new DispatcherPool(eventListener);
		services.add(dispatcherPool);

		muxerPool = new MuxerPool(eventListener, filePathManager, getFFmpeg(), getMkvMerge());
		services.add(muxerPool);

		jobManager = new JobManager(eventListener, nodeManager, dispatcherPool, savedInstance, jobInitiator);
		eventListeners.add(jobManager);
	}

	@Override
	protected void registerThirdParties() {
		registerThirdParty(new FFmpeg(getConfig()));
		registerThirdParty(new FFprobe(getConfig()));
		registerThirdParty(new MkvMerge(getConfig()));
	}

	@Override
	public void shutdown() {
		logger.info("Executing master shutdown routine.\n"
				+ "Ctrl+C again for immediate shutdown (not recommended).\n");

		// save config and make sure to reset current tasks
		for (Node n : nodeManager.getNodes()) {
			for (ClientTask task : n.getAllTasks()) {
				task.getProgress().reset();
			}
		}
		configManager.dump();

		// say goodbye to nodes
		for (Node n : nodeManager.getOnlineNodes()) {
			disconnectNode(n);
		}
		stopServices();
		saveInternalState();
	}

	private void saveInternalState() {
		File file = new File(getConfig().getSavedInstancePath());
		MasterSavedInstance current = new MasterSavedInstance(nodeManager.getNodeHashMap(), jobManager.getJobHashMap());
		MasterSavedInstance.save(file, current);
	}

	/**
	 * Sends a disconnect request to a node, removes the node from the node list and updates the task of the node if it
	 * had any.
	 *
	 * @param n
	 *            The node to remove
	 */
	protected void disconnectNode(Node n) {
		// remove node from list
		jobManager.unassingAll(n);
		nodeManager.removeNode(n);

		MessageSender.send(new AuthMessage(ClusterProtocol.DISCONNECT_ME, n.getUnid()), n.getNodeAddress(),
				n.getNodePort());
		logger.fine(String.format("Disconnected node %s.%n", n.getName()));
	}

	protected void disconnectNode(String unid) {
		disconnectNode(nodeManager.identifySender(unid));
	}

	protected ApiResponse apiDeleteJob(String jobId) {
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
			logger.fine(String.format("Cannot start muxing job %s as some task files are missing !%n", job.getJobName()));

			for (ClientTask missingTask : job.getTodoTasks()) {
				logger.fine(String.format("Missing file '%s' for task %d'.%n", missingTask.getTempFile(),
						missingTask.getTaskId()));
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
			File absoluteTaskFile = FileUtils.getFile(getConfig().getAbsoluteSharedFolder(), task.getFinalFile().getPath());

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
	protected boolean readStatusReport(StatusReport report) {
		NodeState newNodeState = report.status;
		String nodeUnid = report.getUnid();

		// identify node to get it's instance
		Node sender = nodeManager.identifySender(nodeUnid);
		if (sender == null || sender.getStatus() == NodeState.NOT_CONNECTED || report.getTaskReports() == null) {
			return false;
		}
		logger.finer(String.format("Reading status report from %s.%n", sender.getName()));

		readTaskReports(report.getTaskReports());
		// only update if status is changed
		if (sender.getStatus() != newNodeState) {
			sender.setStatus(newNodeState);
			logger.finer(String.format("Node %s is now %s.%n", sender.getName(), newNodeState));
		}

		// remove unassigned tasks
		ArrayList<ClientTask> reportTasks = new ArrayList<>();
		for (TaskReport taskReport : report.getTaskReports()) {
			reportTasks.add(taskReport.getTask());
		}

		jobManager.removeInvalidAssigments(sender, reportTasks);
		this.handle(new Event(EventEnum.WORK_NEEDS_UPDATE));

		return true;
	}

	/**
	 * Reads all task reports and launches an update of the task status and progress
	 *
	 * @param reports
	 *            The reports to read
	 */
	protected void readTaskReports(ArrayList<TaskReport> reports) {
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
			logger.warning(String.format("%s instance not found for node %s.%n", task, node.getName()));
		}
		return true;
	}

	@Override
	public void run() {
		startServices();

		while (!close) {
			try {
				processEvent(eventQueue.take());
			} catch (InterruptedException e) {
			}
		}
	}


	private void processEvent(Event event) {
		switch (event.getCode()) {
		case STATUS_REPORT:
			this.readStatusReport((StatusReport) event.getObject());
			break;
		case JOB_ENCODING_COMPLETED:
			jobEncodingCompleted((Job) event.getObject());
			break;
		case CONFIG_UPDATED:
			configManager.dump();
			break;
		case WORK_NEEDS_UPDATE:
			this.jobManager.updateNodesWork();
			break;
		case WORKER_LOG:
			LogRecordMessage logRecordMessage = (LogRecordMessage) event.getObject();
			clusterLogCollector.add(logRecordMessage.getLogRecord(), logRecordMessage.getUnid());
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

	public MasterAdapter getMasterEventCatcher() {
		return eventListener;
	}

	public MasterConfig getConfig() {
		return configManager.getConfig();
	}

	public NodeManager getNodeManager() {
		return nodeManager;
	}

	public JobManager getJobManager() {
		return jobManager;
	}

	public ClusterLogCollector getClusterLogCollector() {
		return this.clusterLogCollector;
	}


}