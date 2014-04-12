package drfoliberg.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import drfoliberg.common.Status;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.ConnectMessage;
import drfoliberg.common.network.Message;
import drfoliberg.common.network.StatusReport;
import drfoliberg.common.task.Task;

public class Worker implements Runnable {

	Config config;
	private Work workThread;
	private Task currentTask;
	private Status status;

	public WorkerServer workerListener;

	public Worker(String name, InetAddress masterIpAddress, int masterPort, int listenPort) {
		this.config = new Config(masterIpAddress, masterPort, listenPort,"",name);
		this.workerListener = new WorkerServer(this);
		print("initialized not connected to a master server");
	}
	
	public Worker(Config config) {
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
			Message message = new ConnectMessage(config.getUniqueID(), config.getListenPort(), config.getName(), Status.NOT_CONNECTED);
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
					print("something odd happenned");
					break;
				}
			} else {
				print("received invalid message!");
			}
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
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
		this.updateCurrentTaskStatus(100);
		this.updateStatus(Status.FREE);
	}

	public void startWork(Task t) {
		if (this.getStatus() != Status.FREE) {
			print("cannot accept work as i'm not free. Current status: " + this.getStatus());
		} else {
			this.currentTask = t;
			updateStatus(Status.WORKING);
			this.workThread = new Work(this, t, config.getMasterIpAddress());
			this.workThread.start();
		}
	}

	public synchronized void updateCurrentTaskStatus(double progress) {
//		print("updating task's progress to " + progress + "%");
//		Socket socket;
//		try {
//			socket = new Socket(config.getMasterIpAddress(), config.getMasterPort());
//
//			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//			out.flush();
//			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//			TaskReport report = new TaskReport();
//			report.setNode(this.node);
//			report.setProgress(progress);
//			// TODO protect from null pointer
//			report.setJobId(this.node.getCurrentTask().getJobId());
//			report.setTaskId(this.node.getCurrentTask().getTaskId());
//			out.writeObject(report);
//			out.flush();
//			Object o = in.readObject();
//			if (o instanceof Message) {
//				Message m = (Message) o;
//				switch (m.getCode()) {
//				case BYE:
//					socket.close();
//					break;
//				default:
//					socket.close();
//					print("something odd happedned");
//					break;
//				}
//			} else {
//				print("received invalid message!");
//			}
//			socket.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public synchronized void updateStatus(Status statusCode) {
		print("changing worker status to " + statusCode);
		this.status = statusCode;
		if (statusCode == Status.NOT_CONNECTED) {
			ContactMaster contact = new ContactMaster(this);
			Thread mastercontactThread = new Thread(contact);
			mastercontactThread.start();
		} else {
			Socket socket = null;
			try {
				socket = new Socket(getMasterIpAddress(), 1337);

				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				out.flush();
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				StatusReport report = new StatusReport(this.status, config.getUniqueID());
				out.writeObject(report);
				out.flush();
				Object o = in.readObject();
				if (o instanceof Message) {
					Message response = (Message) o;
					if (response.getCode() == ClusterProtocol.BYE) {
						socket.close();
					} else if (response.getCode() == ClusterProtocol.NEW_UNID) {
						out.writeObject(new Message(ClusterProtocol.BYE,config.getUniqueID()));
						out.flush();
						socket.close();
					}
				} else {
					System.out.println("WORKER CONTACT: Could not read what master sent !");
				}
			} catch (IOException e) {
			} catch (ClassNotFoundException e) {
			}
		}

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

	public synchronized Status getStatus() {
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
}
