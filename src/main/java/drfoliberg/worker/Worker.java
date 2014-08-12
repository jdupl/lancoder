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
import drfoliberg.common.RunnableService;
import drfoliberg.common.Service;
import drfoliberg.common.network.Cause;
import drfoliberg.common.network.Routes;
import drfoliberg.common.network.messages.cluster.CrashReport;
import drfoliberg.common.network.messages.cluster.StatusReport;
import drfoliberg.common.status.NodeState;
import drfoliberg.common.status.TaskState;
import drfoliberg.common.task.Task;
import drfoliberg.common.task.audio.AudioEncodingTask;
import drfoliberg.common.task.video.TaskReport;
import drfoliberg.common.task.video.VideoEncodingTask;
import drfoliberg.worker.contacter.ConctactMasterListener;
import drfoliberg.worker.contacter.ContactMasterHttp;
import drfoliberg.worker.converter.ConverterListener;
import drfoliberg.worker.converter.audio.AudioConverterPool;
import drfoliberg.worker.converter.video.VideoWorkThread;
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
		ConverterListener {

	private WorkerConfig config;
	private String configPath;
	private NodeState status;
	private InetAddress address;
	
	private ArrayList<Task> currentTasks = new ArrayList<>();
	private ArrayList<Service> services = new ArrayList<>();
	private VideoWorkThread workThread;
	private AudioConverterPool audioPool;

	public Worker(String configPath) {
		this.configPath = configPath;

        config = WorkerConfig.load(configPath);
		if (config != null) {
			System.err.println("Loaded config from disk !");
		} else {
			// this saves default configuration to disk
			this.config = WorkerConfig.generate(configPath);
		}
		WorkerHttpServer httpServer = new WorkerHttpServer(config.getListenPort(), this, this);
		services.add(httpServer);
		audioPool = new AudioConverterPool(Runtime.getRuntime().availableProcessors(), this);
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

	public void taskDone(VideoEncodingTask t) {
		t.setTaskState(TaskState.TASK_COMPLETED);
		this.updateStatus(NodeState.FREE);
		services.remove(workThread);
	}

	public void stopWork(Task t) {
		// TODO check which task to stop (if many tasks are implemented)
		this.workThread.stop();
		System.err.println("Setting current task to null");
		this.currentTasks.remove(t);
		if (t instanceof VideoEncodingTask) {
			this.updateStatus(NodeState.FREE);
		}
	}

	public synchronized boolean startWork(Task t) {
		if (t instanceof VideoEncodingTask && this.status == NodeState.FREE) {
			VideoEncodingTask vTask = (VideoEncodingTask) t;
			this.workThread = new VideoWorkThread(vTask, this);
			Thread wt = new Thread(workThread);
			wt.start();
			services.add(workThread);
		} else if (t instanceof AudioEncodingTask && this.audioPool.hasFreeConverters()) {
			AudioEncodingTask aTask = (AudioEncodingTask) t;
			audioPool.encode(aTask);
		} else {
			return false;
		}
		t.setTaskState(TaskState.TASK_COMPUTING);
		updateStatus(NodeState.WORKING);
		return true;
	}

	/**
	 * Get a status report of the worker.
	 * 
	 * @return the StatusReport object
	 */
	public StatusReport getStatusReport() {
		return new StatusReport(getStatus(), config.getUniqueID(), getTaskReports());
	}

	/**
	 * Get a task report of the current task.
	 * 
	 * @return null if no current task
	 */
	public ArrayList<TaskReport> getTaskReports() {
		// if worker has no task, return null report
		ArrayList<TaskReport> reports = new ArrayList<TaskReport>();
		for (Task task : currentTasks) {
			TaskReport report = null;
			if (task instanceof VideoEncodingTask) {
				VideoEncodingTask currentTask = (VideoEncodingTask) task;
				currentTask.setTimeElapsed(System.currentTimeMillis() - currentTask.getTimeStarted());
				currentTask.setTimeEstimated(currentTask.getETA());
				currentTask.setProgress(currentTask.getProgress());
				report = new TaskReport(config.getUniqueID(), currentTask);
			} else if (task instanceof AudioEncodingTask) {
				report = new TaskReport(config.getUniqueID(), task);
			}
			if (report != null) {
				reports.add(report);
			}
		}
		return reports;
	}

	public synchronized void updateTaskStatus(Task t, TaskState newState) {
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
			// this.currentTask = null;
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
			notifyHttpMasterStatusChange();
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
				System.err.println("Master responded " + response.getStatusLine().getStatusCode()
						+ " when notifying for new status");
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
	public boolean taskRequest(Task tqm) {
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
	public boolean deleteTask(Task t) {
		for (Task task : currentTasks) {
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
	public void workStarted(Task task) {
		System.err.println("Worker starting task");
		updateTaskStatus(task, TaskState.TASK_COMPUTING);
		this.currentTasks.add(task);
		if (this.status != NodeState.WORKING) {
			updateStatus(NodeState.WORKING);
		}
	}

	@Override
	public void workCompleted(Task task) {
		System.err.println("Worker completed task");
		updateTaskStatus(task, TaskState.TASK_COMPLETED);
		this.currentTasks.remove(task);
		if (currentTasks.size() == 0) {
			updateStatus(NodeState.FREE);
		}
	}

	@Override
	public void workFailed(Task task) {
		System.err.println("Worker failed task " + task.getTaskId());
		updateTaskStatus(task, TaskState.TASK_CANCELED);
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
}
