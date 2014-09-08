package org.lancoder.worker;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.lancoder.common.RunnableService;
import org.lancoder.common.ServerListener;
import org.lancoder.common.Service;
import org.lancoder.common.codecs.Codec;
import org.lancoder.common.network.Cause;
import org.lancoder.common.network.Routes;
import org.lancoder.common.network.messages.ClusterProtocol;
import org.lancoder.common.network.messages.cluster.CrashReport;
import org.lancoder.common.network.messages.cluster.Message;
import org.lancoder.common.network.messages.cluster.StatusReport;
import org.lancoder.common.status.NodeState;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.TaskReport;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.ffmpeg.FFmpegWrapper;
import org.lancoder.worker.contacter.ConctactMasterListener;
import org.lancoder.worker.contacter.ContactMasterObject;
import org.lancoder.worker.converter.ConverterListener;
import org.lancoder.worker.converter.audio.AudioConverterPool;
import org.lancoder.worker.converter.video.VideoWorkThread;
import org.lancoder.worker.server.WorkerObjectServer;
import org.lancoder.worker.server.WorkerServerListener;

public class Worker implements Runnable, ServerListener, WorkerServerListener, ConctactMasterListener,
		ConverterListener {

	private final static String DEFAULT_PATH = new File(System.getProperty("user.home"),
			".config/lancoder/worker_config.json").getPath();

	private WorkerConfig config;
	private String configPath;
	private NodeState status;
	private InetAddress address;
	private int threadCount;
	private ArrayList<Codec> codecs = new ArrayList<>();
	private ArrayList<ClientTask> currentTasks = new ArrayList<>();
	private ArrayList<Service> services = new ArrayList<>();
	private VideoWorkThread workThread;
	private AudioConverterPool audioPool;

	public Worker() {
		this(null);
	}

	public Worker(String suppliedPath) {
		if (suppliedPath == null) {
			this.configPath = DEFAULT_PATH;
		} else {
			this.configPath = suppliedPath;
		}
		config = WorkerConfig.load(configPath);
		if (config != null) {
			System.err.println("Loaded config from disk !");
		} else {
			// this saves default configuration to disk
			this.config = WorkerConfig.generate(configPath);
		}
		this.codecs = FFmpegWrapper.getAvailableCodecs();

		System.out.printf("Detected %d available encoders:\n", codecs.size());
		for (Codec codec : codecs) {
			System.out.println(codec.getPrettyName());
		}
		// Get number of available threads
		this.threadCount = Runtime.getRuntime().availableProcessors();
		System.out.printf("Detected %d threads available.\n", this.threadCount);
		WorkerObjectServer objectServer = new WorkerObjectServer(this, config.getListenPort());
		services.add(objectServer);
		audioPool = new AudioConverterPool(this.threadCount, this);
		services.add(audioPool);
		// Get local ip
		// TODO allow options to override IP detection and enable ipv6
		try {
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			while (n.hasMoreElements()) {
				NetworkInterface e = n.nextElement();
				Enumeration<InetAddress> a = e.getInetAddresses();
				while (a.hasMoreElements()) {
					InetAddress addr = a.nextElement();
					if (!addr.isLoopbackAddress() && (addr instanceof Inet4Address)) {
						address = addr;
						System.out.println("Assuming worker ip is:" + address.getHostAddress());
					}
				}
			}
		} catch (SocketException e) {
			// TODO Perhaps just close worker
			e.printStackTrace();
		}
		print("initialized not connected to a master server");
	}

	public void shutdown() {
		if (this.status != NodeState.NOT_CONNECTED) {
			System.out.println("Sending disconnect notification to master");
			gracefulShutdown();
		}
		int nbServices = services.size();
		print("shutting down " + nbServices + " service(s).");

		for (Service s : services) {
			s.stop();
		}
		config.dump(configPath);
	}

	public void print(String s) {
		System.out.println((getWorkerName().toUpperCase()) + ": " + s);
	}

	public synchronized void stopWork(ClientTask t) {
		// TODO check which task to stop (if many tasks are implemented)
		this.workThread.stop();
		System.err.println("Setting current task to null");
		this.currentTasks.remove(t);
		if (t instanceof ClientVideoTask) {
			this.updateStatus(NodeState.FREE);
		}
	}

	public synchronized boolean startWork(ClientTask t) {
		if (t instanceof ClientVideoTask && this.status == NodeState.FREE) {
			ClientVideoTask vTask = (ClientVideoTask) t;
			this.workThread = new VideoWorkThread(vTask, this);
			Thread wt = new Thread(workThread);
			wt.start();
			services.add(workThread);
		} else if (t instanceof ClientAudioTask && this.audioPool.hasFreeConverters()) {
			ClientAudioTask aTask = (ClientAudioTask) t;
			audioPool.encode(aTask);
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
		for (ClientTask task : currentTasks) {
			TaskReport report = new TaskReport(config.getUniqueID(), task);
			if (report != null) {
				reports.add(report);
			}
		}
		return reports;
	}

	public void updateStatus(NodeState statusCode) {
		if (this.status == NodeState.NOT_CONNECTED && statusCode != NodeState.NOT_CONNECTED) {
			this.stopContactMaster();
		}
		print("changing worker status to " + statusCode);
		this.status = statusCode;
		switch (statusCode) {
		case FREE:
			notifyMasterStatusChange();
			// this.currentTask = null;
			break;
		case WORKING:
		case PAUSED:
			notifyMasterStatusChange();
			break;
		case NOT_CONNECTED:
			// start thread to try to contact master
			startContactMaster();
			break;
		case CRASHED:
			notifyMasterStatusChange();
			break;
		default:
			System.err.println("WORKER: Unhandlded status code while updating status");
			break;
		}
	}

	private void startContactMaster() {
		// ContactMasterHttp contact = new ContactMasterHttp(getMasterIpAddress(), getMasterPort(), this);
		ContactMasterObject contact = new ContactMasterObject(getMasterIpAddress(), getMasterPort(), this);
		Thread mastercontactThread = new Thread(contact);
		mastercontactThread.start();
		this.services.add(contact);
	}

	public void stopContactMaster() {
		System.out.println("Trying to stop contact service");
		for (Service s : this.services) {
			if (s instanceof ContactMasterObject) {
				System.out.println("Found service. Sending stop request.");
				s.stop();
				break;
			}
		}
	}

	@Deprecated
	private void gracefulShutdown() {
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(2000).build();

			URI url = new URI("http", null, this.getCurrentNodeAddress().getHostAddress(), this.getCurrentNodePort(),
					Routes.DISCONNECT_NODE, null, null);
			HttpPost post = new HttpPost(url);
			post.setConfig(defaultRequestConfig);
			// Send request, but don't mind the response
			client.execute(post);
		} catch (IOException e) {
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public synchronized void sendCrashReport(CrashReport report) {
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(2000).build();

			URI url = new URI("http", null, this.getCurrentNodeAddress().getHostAddress(), this.getCurrentNodePort(),
					Routes.NODE_CRASH, null, null);
			HttpPost post = new HttpPost(url);
			post.setConfig(defaultRequestConfig);

			// Send request, but don't mind the response
			client.execute(post);
		} catch (IOException e) {
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public boolean notifyMasterStatusChange() {
		boolean success = false;
		StatusReport report = this.getStatusReport();

		try (Socket s = new Socket(getMasterIpAddress(), getMasterPort())) {
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

	public InetAddress getMasterIpAddress() {
		return config.getMasterIpAddress();
	}

	public int getMasterPort() {
		return config.getMasterPort();
	}

	public NodeState getStatus() {
		return this.status;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public String getWorkerName() {
		return config.getName();
	}

	public void run() {
		for (Service s : services) {
			if (s instanceof RunnableService) {
				Thread t = new Thread((RunnableService) s);
				t.start();
			}
		}
		updateStatus(NodeState.NOT_CONNECTED);
		System.err.println("Started all services");
	}

	public void setUnid(String unid) {
		print("got id " + unid + " from master");
		this.config.setUniqueID(unid);
		this.config.dump(configPath);
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
		for (ClientTask task : currentTasks) {
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
		updateStatus(NodeState.FREE);
	}

	@Override
	public String getCurrentNodeUnid() {
		return this.config.getUniqueID();
	}

	@Override
	public String getCurrentNodeName() {
		return this.getWorkerName();
	}

	@Override
	public int getCurrentNodePort() {
		return this.getListenPort();
	}

	@Override
	public InetAddress getCurrentNodeAddress() {
		return this.address;
	}

	@Override
	public synchronized void workStarted(ClientTask task) {
		task.getProgress().start();
		this.currentTasks.add(task);
		if (this.status != NodeState.WORKING) {
			updateStatus(NodeState.WORKING);
		}
	}

	@Override
	public synchronized void workCompleted(ClientTask task) {
		System.err.println("Worker completed task");
		task.getProgress().complete();
		notifyMasterStatusChange();
		this.currentTasks.remove(task);
		if (currentTasks.size() == 0) {
			updateStatus(NodeState.FREE);
		}
	}

	@Override
	public synchronized void workFailed(ClientTask task) {
		System.err.println("Worker failed task " + task.getTaskId());
		task.getProgress().reset();
		notifyMasterStatusChange();
		this.currentTasks.remove(task);
		if (currentTasks.size() == 0) {
			updateStatus(NodeState.FREE);
		}
	}

	@Override
	public void nodeCrash(Cause cause) {
		// TODO Auto-generated method stub
	}

	@Override
	public WorkerConfig getConfig() {
		return this.config;
	}

	@Override
	public ArrayList<Codec> getAvailableCodecs() {
		return this.codecs;
	}
}
