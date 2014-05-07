package drfoliberg.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import drfoliberg.common.Status;
import drfoliberg.common.network.messages.ConnectMessage;
import drfoliberg.common.network.messages.CrashReport;
import drfoliberg.common.network.messages.Message;
import drfoliberg.common.network.messages.StatusReport;
import drfoliberg.common.task.Task;
import drfoliberg.common.task.TaskReport;

public class Worker implements Runnable {

	final static String WORKER_CONFIG_PATH = "worker_config.json";

	WorkerConfig config;
	private WorkThread workThread;
	private Task currentTask;
	private Status status;
	private CurrentTaskStatus currentTaskStatus;

	public WorkerServer workerListener;

	public Worker(String name, InetAddress masterIpAddress, int masterPort, int listenPort) {
		this.workerListener = new WorkerServer(this);
		config = WorkerConfig.load();
		if (config != null) {
			System.err.println("Loaded config from disk !");
		} else {
			// this saves default configuration to disk
			this.config = WorkerConfig.generate();
		}
		print("initialized not connected to a master server");
	}

	public Worker(WorkerConfig config) {
		this.config = config;
	}

	public void shutdown() {
		print("shutting down");
		workerListener.shutdown();
		Socket socket = null;
		try {
			socket = new Socket(config.getMasterIpAddress(), config.getMasterPort());
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

			// Send a connect message with a status indicating disconnection
			Message message = new ConnectMessage(config.getUniqueID(), config.getListenPort(), config.getName(),
					Status.NOT_CONNECTED);
			out.writeObject(message);
			out.flush();
			Object o = in.readObject();
			if (o instanceof Message) {
				Message m = (Message) o;
				switch (m.getCode()) {
				case BYE:
					socket.close();
					break;
				default:
					socket.close();
					print("something odd happened");
					break;
				}
			} else {
				print("received invalid message!");
			}
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		// TODO halt work thread too!
		print("stopping work thread");
		this.workThread.interrupt();
		Thread.currentThread().interrupt();
	}

	public void print(String s) {
		System.out.println((getWorkerName().toUpperCase()) + ": " + s);
	}

	public void taskDone(Task task, InetAddress masterIp) {
		this.currentTask.setStatus(Status.JOB_COMPLETED);
		this.currentTask.setProgress(100);
		this.updateStatus(Status.FREE);
		this.currentTask = null;
	}

	public synchronized boolean startWork(Task t) {
		if (this.getStatus() != Status.FREE) {
			print("cannot accept work as i'm not free. Current status: " + this.getStatus());
			return false;
		} else {
			this.currentTask = t;
			this.workThread = new WorkThread(this, t, config.getMasterIpAddress());
			this.workThread.start();
			currentTaskStatus.setStartedOn(System.currentTimeMillis());
			currentTask.setStatus(Status.JOB_COMPUTING);
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
		if (getCurrentTask() != null) {
			taskReport = new TaskReport(config.getUniqueID(), this.currentTask);
			Task t = taskReport.getTask();
			t.setProgress(currentTaskStatus.getProgress());
			t.setJobId(getCurrentTask().getJobId());
			t.setTaskId(getCurrentTask().getTaskId());
			t.setFps(currentTaskStatus.getFps());
			t.setTimeElapsed(System.currentTimeMillis() - currentTaskStatus.getStartedOn());
			t.setTimeEstimated(currentTaskStatus.getETA());
		}
		return taskReport;
	}

	public synchronized void updateStatus(Status statusCode) {
		// TODO move this
		print("changing worker status to " + statusCode);
		this.status = statusCode;

		switch (statusCode) {
		case FREE:
		case WORKING:
		case PAUSED:
			notifyMasterStatusChange(statusCode);
			break;
		case NOT_CONNECTED:
			// start thread to try to contact master
			ContactMaster contact = new ContactMaster(this);
			Thread mastercontactThread = new Thread(contact);
			mastercontactThread.start();
			break;
		case CRASHED:
			// cancel current work
			this.currentTask = null;
			break;
		default:
			System.err.println("WORKER: Unhandlded status code while"
					+ " updating status");
			break;
		}
	}

	public synchronized boolean sendCrashReport(CrashReport report) {
		try {
			System.err.println("Sending crash report");
			Socket s = new Socket(config.getMasterIpAddress(), config.getMasterPort());
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			out.flush();
			out.writeObject(report);
			out.flush();
			in.close();
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	public boolean notifyMasterStatusChange(Status status) {
		Socket socket = null;
		boolean success = true;
		try {
			// Init the socket to master
			socket = new Socket(getMasterIpAddress(), config.getMasterPort());
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

			// send report in socket
			out.writeObject(getStatusReport());
			out.flush();
			Object o = in.readObject();
			// check if master sent node new UNID
			if (o instanceof Message) {
				Message response = (Message) o;
				switch (response.getCode()) {
				case BYE:
					// master is closing the socket
					break;
				default:
					System.err.println("WORKER:" + " Master sent unexpected message response");
				}
			} else {
				System.err.println("WORKER CONTACT:" + " Could not read what master sent !");
			}
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			success = false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// pls java
				}
			}
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

	public Status getStatus() {
		return this.status;
	}

	public String getWorkerName() {
		return config.getName();
	}

	public void run() {
		updateStatus(Status.NOT_CONNECTED);
		Thread listerThread = new Thread(workerListener);
		listerThread.start();
	}

	public void setUnid(String unid) {
		print("got id "+ unid + " from master");
		this.config.setUniqueID(unid);
	}

	public Task getCurrentTask() {
		return this.currentTask;
	}

	public synchronized CurrentTaskStatus getCurrentTaskStatus() {
		return currentTaskStatus;
	}

	public synchronized void setCurrentTaskStatus(
			CurrentTaskStatus currentTaskStatus) {
		this.currentTaskStatus = currentTaskStatus;
	}
}
