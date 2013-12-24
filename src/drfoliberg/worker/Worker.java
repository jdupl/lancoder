package drfoliberg.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import drfoliberg.Status;
import drfoliberg.network.ClusterProtocol;
import drfoliberg.network.Message;
import drfoliberg.task.Task;

public class Worker extends Thread {

	private Status status;
	private InetAddress masterIpAddress;
	private String workerName;
	private int masterPort;
	private int listenPort;

	public ListenerWorker workerListener;

	public Worker(String name, InetAddress masterIpAddress, int masterPort, int listenPort) {
		this.workerName = name;
		this.masterPort = masterPort;
		this.listenPort = listenPort;
		this.masterIpAddress = masterIpAddress;
		this.workerListener = new ListenerWorker(this);
		System.out.println("WORKER: Worker " + name + " initialized, not connected to a master server!");
	}

	public void taskDone(Task task, InetAddress masterIp) throws UnknownHostException, IOException,
			ClassNotFoundException {
		this.updateStatus(Status.FREE);
		Socket socket = new Socket(masterIpAddress, masterPort);
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		out.writeObject(new Message(ClusterProtocol.TASK_REPORT));
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
				System.out.println("WORKER: something odd happened");
				break;
			}
		} else {
			System.out.println("MASTER: received invalid message!!");
		}
		socket.close();
	}

	public void startWork(Task t) {
		if (this.getStatus() != Status.FREE) {
			System.out.println("WORKER: cannot accept WORK as i'm not free. Current status: " + this.getStatus());
		} else {
			updateStatus(Status.WORKING);
			Work w = new Work(this, t, masterIpAddress);
			w.start();
		}
	}

	public synchronized void updateStatus(Status statusCode) {
		System.out.println("WORKER: changing status to " + statusCode);
		this.status = statusCode;
		if (statusCode == Status.NOT_CONNECTED) {
			ContactMaster contact = new ContactMaster(this);
			contact.start();
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
		return this.status;
	}

	public String getWorkerName() {
		return workerName;
	}

	public void run() {
		updateStatus(Status.NOT_CONNECTED);
		workerListener.start();
	}

}
