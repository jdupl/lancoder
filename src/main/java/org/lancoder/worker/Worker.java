package org.lancoder.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.lancoder.common.Container;
import org.lancoder.common.FilePathManager;
import org.lancoder.common.Node;
import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.exceptions.InvalidConfigurationException;
import org.lancoder.common.network.cluster.messages.ConnectRequest;
import org.lancoder.common.network.cluster.messages.ConnectResponse;
import org.lancoder.common.network.cluster.messages.CrashReport;
import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.messages.StatusReport;
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
	private InetAddress masterInetAddress = null;
	private int threadCount;

	public Worker(WorkerConfig config) {
		this.config = config;
		bootstrap();
	}

	@Override
	protected void bootstrap() {
		// Get number of available threads
		threadCount = Runtime.getRuntime().availableProcessors();
		System.out.printf("Detected %d threads available.%n", threadCount);
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
		node = new Node(null, this.config.getListenPort(), config.getName(), codecs, threadCount, config.getUniqueID());
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
		audioPool = new AudioConverterPool(threadCount, this, filePathManager, getFFmpeg());
		services.add(audioPool);
		// TODO change to current instance
		videoPool = new VideoConverterPool(1, this, filePathManager, getFFmpeg());
		services.add(videoPool);
		services.add(new WorkerServer(this, config.getListenPort()));
		services.add(new MasterContacter(getMasterInetAddress(), getMasterPort(), this));
	}

	public void shutdown() {
		// if (this.getStatus() != NodeState.NOT_CONNECTED) {
		// System.out.println("Sending disconnect notification to master");
		// gracefulShutdown();
		// }
		this.stop();
	}

	public synchronized void stopWork(ClientTask t) {
		// TODO check which task to stop (if many tasks are implemented)
		this.getCurrentTasks().remove(t);
		audioPool.cancel(t);
		videoPool.cancel(t);
		if (getCurrentTasks().size() == 0) {
			this.updateStatus(NodeState.FREE);
		}
		this.notifyMasterStatusChange();
	}

	private ArrayList<ClientTask> getCurrentTasks() {
		return this.node.getCurrentTasks();
	}

	public boolean startWork(ClientTask task) {
		System.out.println("Received task " + task.getTaskId() + " from master...");
		boolean accepted = true;
		getCurrentTasks().add(task);
		if (task instanceof ClientVideoTask && videoPool.hasFreeConverters()) {
			ClientVideoTask vTask = (ClientVideoTask) task;
			videoPool.handle(vTask);
		} else if (task instanceof ClientAudioTask && this.audioPool.hasFreeConverters()
				&& videoPool.hasFreeConverters()) {
			// video pool must also be free
			ClientAudioTask aTask = (ClientAudioTask) task;
			audioPool.handle(aTask);
		} else {
			getCurrentTasks().remove(task);
			accepted = false;
			System.out.println("Refused task " + task.getTaskId());
		}
		if (accepted) {
			System.out.println("Accepted task " + task.getTaskId());
			task.getProgress().start();
			updateStatus(NodeState.WORKING);
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
		boolean success = false;
		StatusReport report = this.getStatusReport();

		try (Socket s = new Socket(getMasterInetAddress(), getMasterPort())) {
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			out.flush();
			out.writeObject(report);
			out.flush();
			Object o = in.readObject();
			if (o instanceof Message) {
				Message m = (Message) o;
				success = m.getCode() == ClusterProtocol.BYE;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return success;
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
		return startWork(tqm);
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
		task.getProgress().start();
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
