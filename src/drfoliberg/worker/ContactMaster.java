package drfoliberg.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import drfoliberg.common.Node;
import drfoliberg.common.Status;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.Message;
import drfoliberg.common.network.UNID;

public class ContactMaster implements Runnable {

	Worker worker;

	public ContactMaster(Worker worker) {
		this.worker = worker;
	}

	public boolean contactMaster() {
		boolean success = false;
		System.out.println("WORKER CONTACT: trying to contact master server at "
				+ worker.getMasterIpAddress().toString());
		Socket socket = null;
		try {
			socket = new Socket(worker.getMasterIpAddress(), 1337);

			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			Node n = new Node(worker.getWorkerIp(), worker.getListenPort(), worker.getWorkerName());
			out.writeObject(new Message(n));
			out.flush();
			Object o = in.readObject();
			if (o instanceof Message) {
				Message response = (Message) o;
				if (response.getCode() == ClusterProtocol.BYE) {
					socket.close();
				} else if (response.getCode() == ClusterProtocol.NEW_UNID) {
					success = true;
					UNID unid = (UNID) response;
					this.worker.setUnid(unid.getUnid());
					out.writeObject(new Message(ClusterProtocol.BYE));
					out.flush();
					socket.close();
				}
			} else {
				System.out.println("WORKER CONTACT: Could not read what master sent !");
			}
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}
		return success;
	}

	public void run() {
		// while (worker.getStatus() == Status.NOT_CONECTED) {
		boolean success = false;
		while (!success) {
			success = contactMaster();
			if (success) {
				worker.updateStatus(Status.FREE);
				System.out.println("WORKER CONTACT: success contacting master!");
			} else {
				System.out.println("WORKER CONTACT: could not reach the master server. Trying again in 5 seconds.");
				try {
					// TODO check for better way to sleep / handle interrupt
					Thread.currentThread();
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
