package org.lancoder.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.lancoder.common.Container;
import org.lancoder.common.Node;
import org.lancoder.common.RunnableService;
import org.lancoder.common.ServerListener;
import org.lancoder.common.codecs.Codec;
import org.lancoder.common.exceptions.InvalidConfigurationException;
import org.lancoder.common.network.cluster.messages.ConnectMessage;
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
import org.lancoder.worker.server.WorkerObjectServer;
import org.lancoder.worker.server.WorkerServerListener;

public class Worker extends Container implements ServerListener, WorkerServerListener, MasterContacterListener,
		ConverterListener {

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
		// Get codecs
		ArrayList<Codec> codecs = FFmpegWrapper.getAvailableCodecs(config);
		System.out.printf("Detected %d available encoders: %s%n", codecs.size(), codecs);
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
		node = new Node(null, this.config.getListenPort(), config.getName(), codecs, threadCount, config.getUniqueID());
	}

	@Override
	protected void registerThirdParties() {
		this.thirdParties.add(new FFmpeg(config));
	}

	@Override
	protected void registerServices() {
		super.registerServices();
		audioPool = new AudioConverterPool(threadCount, this, config);
		services.add(audioPool);
		videoPool = new VideoConverterPool(1, this, config);
		services.add(videoPool);
		services.add(new WorkerObjectServer(this, config.getListenPort()));
		services.add(new MasterContacter(getMasterInetAddress(), getMasterPort(), this));
	}

	public void shutdown() {
		// if (this.getStatus() != NodeState.NOT_CONNECTED) {
		// System.out.println("Sending disconnect notification to master");
		// gracefulShutdown();
		// }
		this.stop();
	}

	public void print(String s) {
		System.out.println((getWorkerName().toUpperCase()) + ": " + s);
	}

	public synchronized void stopWork(ClientTask t) {
		// TODO check which task to stop (if many tasks are implemented)
		System.err.println("Setting current task to null");
		this.getCurrentTasks().remove(t);
		if (t instanceof ClientVideoTask) {
			this.updateStatus(NodeState.FREE);
		}
	}

	private ArrayList<ClientTask> getCurrentTasks() {
		return this.node.getCurrentTasks();
	}

	public synchronized boolean startWork(ClientTask t) {
		if (t instanceof ClientVideoTask && videoPool.hasFreeConverters()) {
			ClientVideoTask vTask = (ClientVideoTask) t;
			videoPool.handle(vTask);
		} else if (t instanceof ClientAudioTask && this.audioPool.hasFreeConverters() && videoPool.hasFreeConverters()) {
			// video pool must also be free
			ClientAudioTask aTask = (ClientAudioTask) t;
			audioPool.handle(aTask);
		} else {
			return false;
		}
		t.getProgress().start();
		updateStatus(NodeState.WORKING);
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
		print("changing worker status to " + statusCode);
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
			System.err.println("WORKER: Unhandlded status code while updating status");
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
		print("got id " + unid + " from master");
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
	public void serverShutdown(RunnableService server) {
		this.services.remove(server);
	}

	@Override
	public void serverFailure(Exception e, RunnableService server) {
		e.printStackTrace();
	}

	@Override
	public boolean deleteTask(ClientTask t) {
		for (ClientTask task : this.node.getCurrentTasks()) {
			if (task.equals(t)) {
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
	public void receivedUnid(String unid) {
		setUnid(unid);
		String protocol = "http";
		int port = 8080;
		System.out.printf("Worker is now connected to master. Please connect to the webui '%s://%s:%d'.%n", protocol,
				masterInetAddress.getHostAddress(), port);
		updateStatus(NodeState.FREE);
	}

	@Override
	public synchronized void taskStarted(ClientTask task) {
		task.getProgress().start();
		this.getCurrentTasks().add(task);
		if (this.getStatus() != NodeState.WORKING) {
			updateStatus(NodeState.WORKING);
		}
	}

	@Override
	public synchronized void taskCompleted(ClientTask task) {
		System.err.println("Worker completed task");
		task.getProgress().complete();
		notifyMasterStatusChange();
		this.getCurrentTasks().remove(task);
		if (this.getCurrentTasks().isEmpty()) {
			updateStatus(NodeState.FREE);
		}
	}

	@Override
	public synchronized void taskFailed(ClientTask task) {
		System.err.println("Worker failed task " + task.getTaskId());
		task.getProgress().reset();
		notifyMasterStatusChange();
		this.getCurrentTasks().remove(task);
		if (this.getCurrentTasks().isEmpty()) {
			updateStatus(NodeState.FREE);
		}
	}

	@Override
	public ConnectMessage getConnectMessage() {
		return new ConnectMessage(this.node);
	}

	@Override
	public void masterTimeout() {
		System.err.println("Master is disconnected !");
		for (ClientTask task : this.getCurrentTasks()) {
			stopWork(task);
		}
		this.updateStatus(NodeState.NOT_CONNECTED);
	}

}
