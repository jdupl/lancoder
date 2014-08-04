package drfoliberg.worker;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;

import drfoliberg.common.ServerListener;
import drfoliberg.common.Service;
import drfoliberg.common.network.Cause;
import drfoliberg.common.network.Routes;
import drfoliberg.common.network.messages.cluster.CrashReport;
import drfoliberg.common.network.messages.cluster.StatusReport;
import drfoliberg.common.network.messages.cluster.TaskRequestMessage;
import drfoliberg.common.status.NodeState;
import drfoliberg.common.status.TaskState;
import drfoliberg.common.task.video.TaskReport;
import drfoliberg.common.task.video.VideoEncodingTask;
import drfoliberg.worker.contacter.ConctactMasterListener;
import drfoliberg.worker.contacter.ContactMasterHttp;
import drfoliberg.worker.server.WorkerHttpServer;
import drfoliberg.worker.server.WorkerServletListerner;

import org.apache.commons.io.Charsets;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;

public class Worker implements Runnable, ServerListener, WorkerServletListerner, ConctactMasterListener,
		WorkThreadListener {

	WorkerConfig config;

	private String configPath;
	private VideoEncodingTask currentTask;
	private NodeState status;
	private ArrayList<Service> services;
	private WorkerHttpServer server;
	private WorkThread workThread;
	private InetAddress address;

	public Worker(String configPath) {
		this.configPath = configPath;
		this.services = new ArrayList<>();

		config = WorkerConfig.load(configPath);
		if (config != null) {
			System.err.println("Loaded config from disk !");
		} else {
			// this saves default configuration to disk
			this.config = WorkerConfig.generate(configPath);
		}
		server = new WorkerHttpServer(config.getListenPort(), this, this);
		services.add(server);
		print("initialized not connected to a master server");

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
			// TODO Perhaps juste close worker
			e.printStackTrace();
		}
	}

	public Worker(WorkerConfig config) {
		this.config = config;
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

	public void taskDone(VideoEncodingTask t) {
		this.currentTask.setTaskState(TaskState.TASK_COMPLETED);
		this.updateStatus(NodeState.FREE);
		services.remove(workThread);
	}

	public void stopWork(VideoEncodingTask t) {
		// TODO check which task to stop (if many tasks are implemented)
		this.workThread.stop();
		System.err.println("Setting current task to null");
		this.currentTask = null;
		this.updateStatus(NodeState.FREE);
	}

	public synchronized boolean startWork(VideoEncodingTask t) {
		if (this.getStatus() != NodeState.FREE) {
			print("cannot accept work as i'm not free. Current status: " + this.getStatus());
			return false;
		} else {
			updateStatus(NodeState.WORKING);
			this.currentTask = t;
			this.workThread = new WorkThread(t, this);
			Thread wt = new Thread(workThread);
			wt.start();
			services.add(workThread);
			return true;
		}
	}

	/**
	 * Get a status report of the worker.
	 * 
	 * @return the StatusReport object
	 */
	public StatusReport getStatusReport() {
		return new StatusReport(getStatus(), config.getUniqueID(), getTaskReport());
	}

	/**
	 * Get a task report of the current task.
	 * 
	 * @return null if no current task
	 */
	public TaskReport getTaskReport() {
		// if worker has no task, return null report
		TaskReport taskReport = null;
		if (currentTask != null) {
			taskReport = new TaskReport(config.getUniqueID(), this.currentTask);
			VideoEncodingTask t = taskReport.getTask();
			t.setTimeElapsed(System.currentTimeMillis() - currentTask.getTimeStarted());
			t.setTimeEstimated(currentTask.getETA());
			t.setProgress(currentTask.getProgress());
		}
		return taskReport;
	}

	public synchronized void updateTaskStatus(VideoEncodingTask t, TaskState newState) {
		t.setTaskState(newState);
		notifyHttpMasterStatusChange();
	}

	public synchronized void updateStatus(NodeState statusCode) {

		if (this.status == NodeState.NOT_CONNECTED && statusCode != NodeState.NOT_CONNECTED) {
			this.stopContactMaster();
		}
		print("changing worker status to " + statusCode);
		this.status = statusCode;

		switch (statusCode) {
		case FREE:
			notifyHttpMasterStatusChange();
			this.currentTask = null;
			break;
		case WORKING:
		case PAUSED:
			notifyHttpMasterStatusChange();
			break;
		case NOT_CONNECTED:
			// start thread to try to contact master
			startContactMaster();
			break;
		case CRASHED:
			// cancel current work
			notifyHttpMasterStatusChange();
			this.currentTask = null;
			break;
		default:
			System.err.println("WORKER: Unhandlded status code while" + " updating status");
			break;
		}
	}

	private void startContactMaster() {
		ContactMasterHttp contact = new ContactMasterHttp(getMasterIpAddress(), getMasterPort(), this);
		Thread mastercontactThread = new Thread(contact);
		mastercontactThread.start();
		this.services.add(contact);
	}

	public void stopContactMaster() {
		System.out.println("Trying to stop contact service");
		for (Service s : this.services) {
			if (s instanceof ContactMasterHttp) {
				System.out.println("Found service. Sending stop request.");
				s.stop();
				break;
			}
		}
	}

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

	public boolean notifyHttpMasterStatusChange() {
		boolean success = false;
		CloseableHttpClient client = HttpClientBuilder.create().build();
		System.out.println("Creating status report");
		StatusReport report = this.getStatusReport();
		Gson gson = new Gson();
		try {
			StringEntity entity = new StringEntity(gson.toJson(report));
			entity.setContentEncoding(Charsets.UTF_8.toString());
			entity.setContentType(ContentType.APPLICATION_JSON.toString());
			URI url = new URI("http", null, config.getMasterIpAddress().getHostAddress(), config.getMasterPort(),
					Routes.NODE_STATUS, null, null);
			HttpPost post = new HttpPost(url);
			post.setEntity(entity);
			System.out.println("Sending status to master!");
			CloseableHttpResponse response = client.execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {
				success = true;
			} else {
				System.err.println(response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
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

	public String getWorkerName() {
		return config.getName();
	}

	public void run() {
		for (Service s : services) {
			Thread t = new Thread(s);
			t.start();
		}
		updateStatus(NodeState.NOT_CONNECTED);
		System.err.println("Started all services");
	}

	public void setUnid(String unid) {
		print("got id " + unid + " from master");
		this.config.setUniqueID(unid);
		this.config.dump(configPath);
	}

	public VideoEncodingTask getCurrentTask() {
		return this.currentTask;
	}

	@Override
	public boolean taskRequest(TaskRequestMessage tqm) {
		return startWork(tqm.task);
	}

	@Override
	public StatusReport statusRequest() {
		return getStatusReport();
	}

	@Override
	public void serverShutdown(Service server) {
		this.services.remove(server);
	}

	@Override
	public void serverFailure(Exception e, Service server) {
		e.printStackTrace();
	}

	@Override
	public boolean deleteTask(TaskRequestMessage tqm) {
		if (tqm != null && currentTask != null && tqm.task.equals(currentTask)) {
			this.stopWork(currentTask);
			this.updateStatus(NodeState.FREE);
			return true;
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
	public void workStarted(VideoEncodingTask task) {
		System.err.println("Worker starting task");
		updateTaskStatus(task, TaskState.TASK_COMPUTING);
		this.currentTask = task;
		if (this.status != NodeState.WORKING) {
			updateStatus(NodeState.WORKING);
		}
	}

	@Override
	public void workCompleted(VideoEncodingTask task) {
		System.err.println("Worker completed task");
		updateTaskStatus(task, TaskState.TASK_COMPLETED);
		// TODO if worker has many current tasks, implement check if worker has more tasks
		this.currentTask = null;
		updateStatus(NodeState.FREE);
	}

	@Override
	public void workFailed(VideoEncodingTask task) {
		updateTaskStatus(task, TaskState.TASK_CANCELED);
		this.currentTask = null;
		updateStatus(NodeState.FREE);
	}

	@Override
	public void nodeCrash(Cause cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public WorkerConfig getConfig() {
		return this.config;
	}
}
