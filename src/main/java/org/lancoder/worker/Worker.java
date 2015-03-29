package org.lancoder.worker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.lancoder.common.Container;
import org.lancoder.common.FilePathManager;
import org.lancoder.common.Node;
import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.exceptions.InvalidConfigurationException;
import org.lancoder.common.network.MessageSender;
import org.lancoder.common.network.cluster.messages.ConnectRequest;
import org.lancoder.common.network.cluster.messages.ConnectResponse;
import org.lancoder.common.network.cluster.messages.CrashReport;
import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.network.cluster.messages.TaskRequestMessage;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.status.NodeState;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.TaskReport;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.ffmpeg.FFmpegWrapper;
import org.lancoder.worker.contacter.MasterContacter;
import org.lancoder.worker.contacter.MasterContacterListener;
import org.lancoder.worker.converter.ConverterListener;
import org.lancoder.worker.converter.audio.AudioConverterPool;
import org.lancoder.worker.converter.video.VideoConverterPool;
import org.lancoder.worker.server.WorkerServer;
import org.lancoder.worker.server.WorkerServerListener;

public class Worker extends Container implements WorkerServerListener, MasterContacterListener, ConverterListener {

	private Node node;
	private WorkerConfig config;
	private AudioConverterPool audioPool;
	private VideoConverterPool videoPool;
	private MasterContacter masterContacter;
	private InetAddress masterInetAddress = null;
	private int threadLimit;
	private TaskHandlerPool taskHandler;

	public Worker(WorkerConfig config) {
		this.config = config;
		bootstrap();
	}

	@Override
	protected void bootstrap() {
		// Get number of available threads
		threadLimit = Runtime.getRuntime().availableProcessors();
		System.out.printf("Detected %d threads available.%n", threadLimit);
		// Parse master ip address or host name
		try {
			this.masterInetAddress = InetAddress.getByName(config.getMasterIpAddress());
		} catch (UnknownHostException e) {
			throw new InvalidConfigurationException(String.format("Master's host name '%s' could not be resolved !"
					+ "\nOriginal exception: '%s'", config.getMasterIpAddress(), e.getMessage()));
		}
		super.bootstrap();
		// Get codecs
		ArrayList<CodecEnum> codecs = FFmpegWrapper.getAvailableCodecs(getFFmpeg());
		System.out.printf("Detected %d available encoders: %s%n", codecs.size(), codecs);
		node = new Node(null, this.config.getListenPort(), config.getName(), codecs, threadLimit, config.getUniqueID());
	}

	@Override
	protected void registerThirdParties() {
		registerThirdParty(new FFmpeg(config));
	}

	@Override
	protected void registerServices() {
		super.registerServices();
		filePathManager = new FilePathManager(config);
		// TODO change to current instance
		audioPool = new AudioConverterPool(threadLimit, this, filePathManager, getFFmpeg());
		services.add(audioPool);
		// TODO change to current instance
		videoPool = new VideoConverterPool(1, this, filePathManager, getFFmpeg());
		services.add(videoPool);
		taskHandler = new TaskHandlerPool(this);
		services.add(taskHandler);
		services.add(new WorkerServer(this, config.getListenPort()));
		masterContacter = new MasterContacter(getMasterInetAddress(), getMasterPort(), this);
		services.add(masterContacter);
	}

	public void shutdown() {
		// if (this.getStatus() != NodeState.NOT_CONNECTED) {
		// System.out.println("Sending disconnect notification to master");
		// gracefulShutdown();
		// }
		this.stop();
	}

	public synchronized void stopWork(ClientTask t) {
		this.getCurrentTasks().remove(t);
		audioPool.cancel(t);
		videoPool.cancel(t);
		if (getCurrentTasks().size() == 0) {
			this.updateStatus(NodeState.FREE);
		}
		this.notifyMasterStatusChange();
	}

	private ArrayList<ClientTask> getCurrentTasks() {
		return node.getCurrentTasks();
	}

	private ArrayList<ClientTask> getPendingTasks() {
		return node.getPendingTasks();
	}

	public synchronized boolean startWork(ClientTask task) {
		System.out.println("Received task " + task.getTaskId() + " from master...");
		boolean accepted = false;
		int totalUsedThreads = videoPool.getActiveThreadCount() + audioPool.getActiveThreadCount();

		if (getPendingTasks().size() != 1) {
			System.out.println("Refusing task because worker has " + (getPendingTasks().size() - 1) + " other pending tasks.");
		} else if (task instanceof ClientVideoTask && videoPool.hasFreeConverters() && totalUsedThreads < threadLimit) {
			ClientVideoTask vTask = (ClientVideoTask) task;
			videoPool.handle(vTask);
			accepted = true;
		} else if (task instanceof ClientAudioTask && this.audioPool.hasFreeConverters()
				&& totalUsedThreads < threadLimit) {
			ClientAudioTask aTask = (ClientAudioTask) task;
			audioPool.handle(aTask);
			accepted = true;
		}
		if (accepted) {
			System.out.println("Accepted task " + task.getTaskId());
			task.start();
			node.confirm(task);
			MessageSender.send(new TaskRequestMessage(task, ClusterProtocol.TASK_ACCEPTED), getMasterInetAddress(),
					getMasterPort());
		} else {
			node.removeTask(task);
			System.out.println("Refused task " + task.getTaskId());
			MessageSender.send(new TaskRequestMessage(task, ClusterProtocol.TASK_REFUSED), getMasterInetAddress(),
					getMasterPort());
			//notifyMasterStatusChange();
		}
		return true;
	}

	/**
	 * Get a status report of the worker.
	 * 
	 * @return the StatusReport object
	 */
	public synchronized StatusReport getStatusReport() {
		return new StatusReport(getStatus(), config.getUniqueID(), getTaskReports());
	}

	/**
	 * Get a task report of the current task.
	 * 
	 * @return null if no current task
	 */
	public ArrayList<TaskReport> getTaskReports() {
		ArrayList<TaskReport> reports = new ArrayList<TaskReport>();
		for (ClientTask task : this.getCurrentTasks()) {
			TaskReport report = new TaskReport(config.getUniqueID(), task);
			if (report != null) {
				reports.add(report);
			}
		}
		return reports;
	}

	private void setStatus(NodeState state) {
		this.node.setStatus(state);
	}

	public void updateStatus(NodeState statusCode) {
		this.setStatus(statusCode);

		switch (statusCode) {
		case FREE:
			notifyMasterStatusChange();
			break;
		case WORKING:
		case PAUSED:
			notifyMasterStatusChange();
			break;
		case NOT_CONNECTED:
			break;
		case CRASHED:
			notifyMasterStatusChange();
			break;
		default:
			System.err.println("Unhandlded status code while updating status");
			break;
		}
	}

	public synchronized void sendCrashReport(CrashReport report) {
		throw new UnsupportedOperationException();
	}

	public boolean notifyMasterStatusChange() {
		StatusReport report = this.getStatusReport();
		Message response = MessageSender.send(report, getMasterInetAddress(), getMasterPort());
		return (response != null && response.getCode() == ClusterProtocol.BYE);
	}

	public int getListenPort() {
		return config.getListenPort();
	}

	public InetAddress getMasterInetAddress() {
		return masterInetAddress;
	}

	public int getMasterPort() {
		return config.getMasterPort();
	}

	public NodeState getStatus() {
		return this.node.getStatus();
	}

	public int getThreadCount() {
		return this.node.getThreadCount();
	}

	public String getWorkerName() {
		return config.getName();
	}

	public void run() {
		updateStatus(NodeState.NOT_CONNECTED);
		startServices();
	}

	public void setUnid(String unid) {
		this.config.setUniqueID(unid);
		this.config.dump();
	}

	@Override
	public boolean taskRequest(ClientTask tqm) {
		node.addPendingTask(tqm);
		taskHandler.handle(tqm);
		return true;
	}

	@Override
	public StatusReport statusRequest() {
		return getStatusReport();
	}

	@Override
	public boolean deleteTask(ClientTask t) {
		for (ClientTask task : this.node.getCurrentTasks()) {
			if (task.equals(t)) {
				System.out.printf("Stopping task %d of job %s as Master requested !%n", task.getTaskId(),
						task.getJobId());
				stopWork(task);
				return true;
			}
		}
		return false;
	}

	@Override
	public void shutdownWorker() {
		System.err.println("Received shutdown request from api !");
		this.shutdown();
	}

	@Override
	public void onConnectResponse(ConnectResponse responseMessage) {
		String unid = responseMessage.getNewUnid();
		if (unid != null && !unid.isEmpty()) {
			setUnid(unid);
			String protocol = responseMessage.getWebuiProtocol();
			int port = responseMessage.getWebuiPort();
			System.out.printf("Worker is now connected to master. Please connect to the webui at '%s://%s:%d'.%n",
					protocol, masterInetAddress.getHostAddress(), port);
			updateStatus(NodeState.FREE);
		} else {
			System.err.println("Received empty or invalid UNID from master.");
		}
	}

	@Override
	public synchronized void taskStarted(ClientTask task) {
		task.start();
		if (this.getStatus() != NodeState.WORKING) {
			updateStatus(NodeState.WORKING);
		}
	}

	@Override
	public synchronized void taskCompleted(ClientTask task) {
		System.out.println("Completed task " + task.getTaskId());
		task.getProgress().complete();
		notifyAndRemove(task);
	}

	@Override
	public synchronized void taskCancelled(ClientTask task) {
		task.getProgress().reset();
		System.out.println("Cancelled task " + task.getTaskId());
		notifyAndRemove(task);
	}

	@Override
	public synchronized void taskFailed(ClientTask task) {
		task.fail();
		System.out.println("Failed task " + task.getTaskId());
		notifyAndRemove(task);
	}

	private void notifyAndRemove(ClientTask task) {
		notifyMasterStatusChange(); // Master will update the task's status
		this.getCurrentTasks().remove(task);
		if (this.getCurrentTasks().isEmpty()) {
			updateStatus(NodeState.FREE);
		}
	}

	@Override
	public ConnectRequest getConnectMessage() {
		return new ConnectRequest(this.node);
	}

	@Override
	public void masterTimeout() {
		System.err.println("Lost connection to master !");
		for (ClientTask task : this.getCurrentTasks()) {
			stopWork(task);
		}
		this.updateStatus(NodeState.NOT_CONNECTED);
	}

}
