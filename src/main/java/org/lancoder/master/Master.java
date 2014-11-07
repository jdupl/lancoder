package org.lancoder.master;

import java.io.File;
import java.util.ArrayList;

import org.lancoder.common.Container;
import org.lancoder.common.Node;
import org.lancoder.common.RunnableService;
import org.lancoder.common.ServerListener;
import org.lancoder.common.events.Event;
import org.lancoder.common.events.EventListener;
import org.lancoder.common.job.Job;
import org.lancoder.common.network.cluster.messages.AuthMessage;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.network.messages.web.ApiJobRequest;
import org.lancoder.common.network.messages.web.ApiResponse;
import org.lancoder.common.status.JobState;
import org.lancoder.common.status.NodeState;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.TaskReport;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.common.third_parties.FFprobe;
import org.lancoder.common.third_parties.MkvMerge;
import org.lancoder.common.utils.FileUtils;
import org.lancoder.master.api.node.MasterServer;
import org.lancoder.master.api.web.ApiServer;
import org.lancoder.master.checker.NodeCheckerService;
import org.lancoder.master.dispatcher.DispatchItem;
import org.lancoder.master.dispatcher.DispatcherPool;
import org.lancoder.muxer.MuxerListener;
import org.lancoder.muxer.MuxerPool;

public class Master extends Container implements MuxerListener, ServerListener, JobInitiatorListener, EventListener {

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
		nodeManager = new NodeManager(this, config, savedInstance);
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
		muxerPool = new MuxerPool(this, config.getAbsoluteSharedFolder());
		services.add(muxerPool);
		jobManager = new JobManager(this, nodeManager, dispatcherPool, savedInstance);
	}

	@Override
	protected void registerThirdParties() {
		this.thirdParties.add(new FFmpeg(config));
		this.thirdParties.add(new FFprobe(config));
		this.thirdParties.add(new MkvMerge(config));
	}

	public void shutdown() {
		// save config and make sure to reset current tasks
		for (Node n : nodeManager.getNodes()) {
			for (ClientTask task : n.getCurrentTasks()) {
				task.getProgress().reset();
			}
		}
		config.dump();
		// say goodbye to nodes
		for (Node n : nodeManager.getOnlineNodes()) {
			disconnectNode(n);
		}
		stopServices();
		MasterSavedInstance current = new MasterSavedInstance(nodeManager.getNodeHashMap(), jobManager.getJobHashMap());
		MasterSavedInstance.save(new File(config.getSavedInstancePath()), current);
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
		nodeManager.removeNode(n);
		dispatcherPool.handle(new DispatchItem(new AuthMessage(ClusterProtocol.DISCONNECT_ME, n.getUnid()), n));
		System.out.printf("Disconnected node %s%n.", n.getName());
	}

	public void disconnectNode(String unid) {
		disconnectNode(nodeManager.identifySender(unid));
	}

	@Deprecated
	public ArrayList<Job> getJobs() {
		return jobManager.getJobs();
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
	public boolean readStatusReport(StatusReport report) {
		NodeState s = report.status;
		String unid = report.getUnid();
		Node sender = nodeManager.identifySender(unid);
		if (sender == null || sender.getStatus() == NodeState.NOT_CONNECTED) {
			return false;
		}

		if (report.getTaskReports() != null) {
			readTaskReports(report.getTaskReports());
		}
		// only update if status is changed
		if (sender.getStatus() != report.status) {
			sender.setStatus(s);
			jobManager.updateNodesWork();
		}
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
			ClientTask reportTask = report.getTask();
			ClientTask actualTask = null;
			String nodeId = report.getUnid();
			Node sender = nodeManager.identifySender(nodeId);

			if (sender == null || !sender.hasTask(reportTask)) {
				System.err.printf("MASTER: Bad task update from node.");
			} else {
				for (ClientTask t : sender.getCurrentTasks()) {
					if (t.equals(reportTask)) {
						actualTask = t;
					}
				}
				// TaskState oldState = actualTask.getProgress().getTaskState();
				actualTask.setProgress(reportTask.getProgress());
				// if (!oldState.equals(actualTask.getProgress().getTaskState())) {
				// System.out.printf("Updating task id %d from %s to %s\n", reportTask.getTaskId(), oldState,
				// actualTask.getProgress().getTaskState());
				// }
				jobManager.taskUpdated(actualTask, sender);
			}
		}
	}

	public void run() {
		startServices();
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
	public void newJob(Job job) {
		this.jobManager.addJob(job);
	}

	@Override
	public void jobMuxingStarted(Job job) {
		job.setJobStatus(JobState.JOB_MUXING);
	}

	@Override
	public void jobMuxingCompleted(Job job) {
		System.out.printf("Job %s finished muxing !\n", job.getJobName());
		job.setJobStatus(JobState.JOB_COMPLETED);
	}

	@Override
	public void jobMuxingFailed(Job job) {
		System.err.printf("Muxing failed for job %s\n", job.getJobName());
		// TODO
	}

	@Override
	public void handle(Event event) {
		switch (event.getCode()) {
		case NODE_DISCONNECTED:
			nodeManager.removeNode((Node) event.getObject());
			break;
		case STATUS_REPORT:
			this.readStatusReport((StatusReport) event.getObject());
			break;
		case WORK_NEEDS_UPDATE:
			this.jobManager.updateNodesWork();
			break;
		case JOB_ENCODING_COMPLETED:
			jobEncodingCompleted((Job) event.getObject());
			break;
		case CONFIG_UPDATED:
			this.config.dump();
			break;
		default:
			break;
		}
	}
}