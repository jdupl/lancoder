package drfoliberg.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import drfoliberg.Status;
import drfoliberg.network.ClusterProtocol;
import drfoliberg.network.Message;

public class ListenerWorker extends Thread {

	private Worker worker;

	public ListenerWorker(Worker w) {
		this.worker = w;
	}

	public void run() {
		try {
			ServerSocket server = new ServerSocket(worker.getListenPort());
			while (true) {
				System.out.println("WORKER LISTENER: listening for messages from master!");
				Socket s = server.accept();

				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				out.flush();
				ObjectInputStream in = new ObjectInputStream(s.getInputStream());

				Object o = in.readObject();

				if (o instanceof Message) {
					Message m = (Message) o;

					switch (m.getCode()) {

					case ClusterProtocol.DISCONNECT_ME:
						System.out.println("WORKER LISTENER: master wants me to disconnect !");
						out.writeObject(new Message(ClusterProtocol.BYE));
						out.flush();
						s.close();
						worker.updateStatus(Status.NOT_CONECTED);
						break;
					case ClusterProtocol.TASK_REQUEST:
						System.out.println("WORKER LISTENER: received task from master");
						worker.startWork(m.getTask());
						System.out.println("WORKER LISTENER: sending to master the status of the task");
						if (worker.getStatus() == Status.WORKING) {
							out.writeObject(new Message(ClusterProtocol.TASK_ACCEPTED));
						} else {
							out.writeObject(new Message(ClusterProtocol.TASK_REFUSED));
						}
						break;

					case ClusterProtocol.BYE:
						s.close();
						break;
					default:
						out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
						break;
					}
				} else {
					System.out.println("WORKER LISTENER: could not read packet !");
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
