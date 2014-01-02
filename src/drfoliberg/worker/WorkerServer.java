package drfoliberg.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.xml.internal.ws.util.StringUtils;

import drfoliberg.common.Status;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.Message;

public class WorkerServer extends Thread {

	private Worker worker;

	public WorkerServer(Worker w) {
		this.worker = w;
	}
	public void print(String s) {
		System.out.println(StringUtils.capitalize(worker.getWorkerName().toUpperCase()) + " SERVER: " + s);
	}

	public void run() {
		try {
			ServerSocket server = new ServerSocket(worker.getListenPort());
			while (true) {
				print("listening for messages from master!");
				Socket s = server.accept();

				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				out.flush();
				ObjectInputStream in = new ObjectInputStream(s.getInputStream());

				Object o = in.readObject();

				if (o instanceof Message) {
					Message m = (Message) o;

					switch (m.getCode()) {

					case DISCONNECT_ME:
						print("master wants me to disconnect !");
						out.writeObject(new Message(ClusterProtocol.BYE));
						out.flush();
						s.close();
						worker.updateStatus(Status.NOT_CONNECTED);
						break;
					case TASK_REQUEST:
						print("received task from master");
						worker.startWork(m.getTask());
						print("sending to master the status of the task");
						if (worker.getStatus() == Status.WORKING) {
							out.writeObject(new Message(ClusterProtocol.TASK_ACCEPTED));
						} else {
							out.writeObject(new Message(ClusterProtocol.TASK_REFUSED));
						}
						break;

					case BYE:
						s.close();
						break;
					default:
						out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
						break;
					}
				} else {
					print("could not read packet !");
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
