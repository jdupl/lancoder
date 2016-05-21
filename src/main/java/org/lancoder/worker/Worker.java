package org.lancoder.worker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.lancoder.common.Container;
import org.lancoder.common.FilePathManager;
import org.lancoder.common.Node;
import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.config.Config;
import org.lancoder.common.config.ConfigManager;
import org.lancoder.common.exceptions.InvalidConfigurationException;
import org.lancoder.common.logging.ClusterLogStagingHandler;
import org.lancoder.common.network.MessageSender;
import org.lancoder.common.network.cluster.messages.ConnectRequest;
import org.lancoder.common.network.cluster.messages.ConnectResponse;
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
import org.lancoder.worker.logging.LogSenderPool;
import org.lancoder.worker.server.WorkerServer;
import org.lancoder.worker.server.WorkerServerListener;

public class Worker extends Container implements WorkerServerListener, MasterContacterListener, ConverterListener {

	private Node node;
	private AudioConverterPool audioPool;
	private VideoConverterPool videoPool;
	private MasterContacter masterContacter;
	private InetAddress masterInetAddress = null;
	private int threadLimit;
	private TaskHandlerPool taskHandler;
	private ConfigManager<WorkerConfig> configManager;

	@Override
	public void setConfigManager(ConfigManager<? extends Config> config) {
		@SuppressWarnings("unchecked")
		ConfigManager<WorkerConfig> manager = (ConfigManager<WorkerConfig>) config;
		this.configManager = manager;
	}

	@Override
	public Class<? extends Config> getConfigClass() {
		return WorkerConfig.class;
	}

	public WorkerConfig getConfig() {
		return this.configManager.getConfig();
	}

	@Override
	public void bootstrap() {
		Logger logger = Logger.getLogger("lancoder");

		// Get number of available threads
		threadLimit = Runtime.getRuntime().availableProcessors();
		logger.fine(String.format("Detected %d threads available.%n", threadLimit));

		// Parse master ip address or host name
		try {
			this.masterInetAddress = InetAddress.getByName(getConfig().getMasterIpAddress());
		} catch (UnknownHostException e) {
			throw new InvalidConfigurationException(String.format("Master's host name '%s' could not be resolved !"
					+ "\nOriginal exception: '%s'", getConfig().getMasterIpAddress(), e.getMessage()));
		}
		super.bootstrap();
		// Get codecs
		ArrayList<CodecEnum> codecs = FFmpegWrapper.getAvailableCodecs(getFFmpeg());
		node = new Node(null, getConfig().getListenPort(), getConfig().getName(), codecs, threadLimit, getConfig()
				.getUniqueID());

		logger.fine(String.format("Detected %d available encoders: %s%n", codecs.size(), codecs));
	}

	@Override
	protected void registerThirdParties() {
		registerThirdParty(new FFmpeg(getConfig()));
	}

	@Override
	protected void registerServices() {
		super.registerServices();
		filePathManager = new FilePathManager(getConfig());
		// TODO change to current instance
		audioPool = new AudioConverterPool(threadLimit, this, filePathManager, getFFmpeg());
		services.add(audioPool);

		// TODO change to current instance
		videoPool = new VideoConverterPool(1, this, filePathManager, getFFmpeg());
		services.add(videoPool);

		taskHandler = new TaskHandlerPool(this);
		services.add(taskHandler);

		services.add(new WorkerServer(this, getConfig().getListenPort()));

		masterContacter = new MasterContacter(getMasterInetAddress(), getMasterPort(), this);
		services.add(masterContacter);

		LogSenderPool logRecordSender = new LogSenderPool(this);
		services.add(logRecordSender);
		logger.addHandler(new ClusterLogStagingHandler(logRecordSender));
	}

	@Override
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
		Logger logger = Logger.getLogger("lancoder");
		logger.fine(String.format("Received %s from master.%n", task.toString()));

		boolean accepted = false;
		int totalUsedThreads = videoPool.getActiveThreadCount() + audioPool.getActiveThreadCount();

		if (getPendingTasks().size() != 1) {
			logger.fine("Refusing task because worker has " + (getPendingTasks().size() - 1) + " other pending tasks.\n");
		} else if (task instanceof ClientVideoTask && videoPool.hasFreeConverters() && totalUsedThreads < threadLimit) {
			ClientVideoTask vTask = (ClientVideoTask) task;
			videoPool.add(vTask);
			accepted = true;
		} else if (task instanceof ClientAudioTask && this.audioPool.hasFreeConverters()
				&& totalUsedThreads < threadLimit) {
			ClientAudioTask aTask = (ClientAudioTask) task;
			audioPool.add(aTask);
			accepted = true;
		}

		if (accepted) {
			logger.fine(String.format("Accepted %s.%n", task));

			task.start();
			node.confirm(task);
			MessageSender.send(new TaskRequestMessage(task, ClusterProtocol.TASK_ACCEPTED), getMasterInetAddress(),
					getMasterPort());
		} else {
			logger.fine(String.format("Refused %s.%n", task));

			node.removeTask(task);
			MessageSender.send(new TaskRequestMessage(task, ClusterProtocol.TASK_REFUSED), getMasterInetAddress(),
					getMasterPort());
		}
		return true;
	}

	/**
	 * Get a status report of the worker.
	 *
	 * @return the StatusReport object
	 */
	public synchronized StatusReport getStatusReport() {
		return new StatusReport(getStatus(), getConfig().getUniqueID(), getTaskReports());
	}

	/**
	 * Get a task report of the current task.
	 *
	 * @return null if no current task
	 */
	public ArrayList<TaskReport> getTaskReports() {
		ArrayList<TaskReport> reports = new ArrayList<TaskReport>();

		for (ClientTask task : this.getCurrentTasks()) {
			TaskReport report = new TaskReport(getConfig().getUniqueID(), task);

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
			Logger logger = Logger.getLogger("lancoder");
			logger.warning(String.format("Caught unknown status code '%s' while updating status%n", statusCode));
			break;
		}
	}

	public boolean notifyMasterStatusChange() {
		StatusReport report = this.getStatusReport();
		Message response = MessageSender.send(report, getMasterInetAddress(), getMasterPort());
		return (response != null && response.getCode() == ClusterProtocol.BYE);
	}

	public int getListenPort() {
		return getConfig().getListenPort();
	}

	public InetAddress getMasterInetAddress() {
		return masterInetAddress;
	}

	public int getMasterPort() {
		return getConfig().getMasterPort();
	}

	@Override
	public NodeState getStatus() {
		if (this.node == null) {
			return NodeState.NOT_CONNECTED;
		}
		return this.node.getStatus();
	}

	public int getThreadCount() {
		return this.node.getThreadCount();
	}

	public String getWorkerName() {
		return getConfig().getName();
	}

	@Override
	public void run() {
		updateStatus(NodeState.NOT_CONNECTED);
		startServices();
	}

	public void setUnid(String unid) {
		this.getConfig().setUniqueID(unid);
		configManager.dump();
	}

	@Override
	public boolean taskRequest(ClientTask tqm) {
		node.addPendingTask(tqm);
		taskHandler.add(tqm);
		return true;
	}

	@Override
	public StatusReport statusRequest() {
		return getStatusReport();
	}

	@Override
	public boolean deleteTask(ClientTask t) {
		Logger logger = Logger.getLogger("lancoder");

		for (ClientTask task : this.node.getCurrentTasks()) {
			if (task.equals(t)) {
				logger.fine(String.format("Stopping task %d of job %s as master requested !%n", task.getTaskId(),
						task.getJobId()));

				stopWork(task);
				return true;
			}
		}
		return false;
	}

	@Override
	public void shutdownWorker() {
		Logger logger = Logger.getLogger("lancoder");
		logger.info("Received shutdown request from api !\n");
		this.shutdown();
	}

	@Override
	public void onConnectResponse(ConnectResponse responseMessage) {
		Logger logger = Logger.getLogger("lancoder");
		String unid = responseMessage.getNewUnid();

		if (unid != null && !unid.isEmpty()) {
			setUnid(unid);
			String protocol = responseMessage.getWebuiProtocol();
			int port = responseMessage.getWebuiPort();

			logger.info(String.format("Worker is now connected to master. Please connect to the webui at '%s://%s:%d'.%n",
					protocol, masterInetAddress.getHostAddress(), port));
			updateStatus(NodeState.FREE);
		} else {
			logger.severe("Received empty or invalid UNID from master.\n");
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
		Logger logger = Logger.getLogger("lancoder");
		logger.fine(String.format("Completed %s.%n", task));

		task.getProgress().complete();
		notifyAndRemove(task);
	}

	@Override
	public synchronized void taskCancelled(ClientTask task) {
		Logger logger = Logger.getLogger("lancoder");
		logger.fine(String.format("Cancelled %s.%n", task));

		task.getProgress().reset();
		notifyAndRemove(task);
	}

	@Override
	public synchronized void taskFailed(ClientTask task) {
		Logger logger = Logger.getLogger("lancoder");
		logger.fine(String.format("Failed %s.%n", task));

		task.fail();
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
		Logger logger = Logger.getLogger("lancoder");
		logger.info("Lost connection to master !%n");

		for (ClientTask task : this.getCurrentTasks()) {
			stopWork(task);
		}
		this.updateStatus(NodeState.NOT_CONNECTED);
	}

}
