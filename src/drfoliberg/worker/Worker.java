package drfoliberg.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import drfoliberg.common.Node;
import drfoliberg.common.Status;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.Message;
import drfoliberg.common.network.StatusReport;
import drfoliberg.common.network.TaskReport;
import drfoliberg.common.task.Task;

public class Worker extends Thread {

	// private Status status;
	private InetAddress workerIp;
	private InetAddress masterIpAddress;
	// private String workerName;
	private int masterPort;
	private int listenPort;
	private Node node;

	public WorkerServer workerListener;

	public Worker(String name, InetAddress masterIpAddress, int masterPort,
			int listenPort) {
		this.node = new Node(getWorkerIp(), listenPort, name);
		this.masterPort = masterPort;
		this.listenPort = listenPort;
		this.masterIpAddress = masterIpAddress;
		this.workerListener = new WorkerServer(this);

		node.setUnid("");
		try {
			// TODO better reliability
			this.workerIp = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
		}
		print("initialized not connected to a master server");
	}

	public void print(String s) {
		System.out.println((getWorkerName().toUpperCase()) + ": " + s);
	}

	public void taskDone(Task task, InetAddress masterIp)
			throws UnknownHostException, IOException, ClassNotFoundException {
		
		Socket socket = new Socket(masterIpAddress, masterPort);
		ObjectOutputStream out = new ObjectOutputStream(
				socket.getOutputStream());
		out.flush();
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		TaskReport report = new TaskReport();
		report.setNode(this.node);
		report.setProgress(100);
		report.setJobId(task.getJobId());
		report.setTaskId(task.getTaskId());
		out.writeObject(report);
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
				print("something odd happedned");
				break;
			}
		} else {
			print("received invalid message!");
		}
		socket.close();
		this.updateStatus(Status.FREE);
	}

	public void startWork(Task t) {
		if (this.getStatus() != Status.FREE) {
			print("cannot accept work as i'm not free. Current status: "
					+ this.getStatus());
		} else {
			updateStatus(Status.WORKING);
			Work w = new Work(this, t, masterIpAddress);
			w.start();
		}
	}

	public synchronized void updateStatus(Status statusCode) {
		print("changing status to " + statusCode);
		this.node.setStatus(statusCode);
		if (statusCode == Status.NOT_CONNECTED) {
			ContactMaster contact = new ContactMaster(this);
			contact.start();
		} else {
			Socket socket = null;
			try {
				socket = new Socket(getMasterIpAddress(), 1337);

				ObjectOutputStream out = new ObjectOutputStream(
						socket.getOutputStream());
				out.flush();
				ObjectInputStream in = new ObjectInputStream(
						socket.getInputStream());
				StatusReport report = new StatusReport(this.node);
				report.setNode(this.node);
				out.writeObject(report);
				out.flush();
				Object o = in.readObject();
				if (o instanceof Message) {
					Message response = (Message) o;
					if (response.getCode() == ClusterProtocol.BYE) {
						socket.close();
					} else if (response.getCode() == ClusterProtocol.NEW_UNID) {
						out.writeObject(new Message(ClusterProtocol.BYE));
						out.flush();
						socket.close();
					}
				} else {
					System.out
							.println("WORKER CONTACT: Could not read what master sent !");
				}
			} catch (IOException e) {
			} catch (ClassNotFoundException e) {
			}
		}

	}

	public int getListenPort() {
		return listenPort;
	}

	public InetAddress getMasterIpAddress() {
		return masterIpAddress;
	}

	public int getMasterPort() {
		return masterPort;
	}

	public synchronized Status getStatus() {
		return this.node.getStatus();
	}

	public String getWorkerName() {
		return node.getName();
	}

	public void run() {
		updateStatus(Status.NOT_CONNECTED);
		workerListener.start();
	}

	public InetAddress getWorkerIp() {
		return workerIp;
	}

	public void setUnid(String unid) {
		this.node.setUnid(unid);
	}
}
