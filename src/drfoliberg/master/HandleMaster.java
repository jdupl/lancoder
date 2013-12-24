package drfoliberg.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import drfoliberg.network.ClusterProtocol;
import drfoliberg.network.Message;

public class HandleMaster extends Thread {

	ObjectInputStream in;
	ObjectOutputStream out;
	Socket s;
	Master master;

	public HandleMaster(Socket s, Master master) {
		this.master = master;
		this.s = s;
	}

	public void run() {
		boolean close = false;
		try {
			out = new ObjectOutputStream(s.getOutputStream());
			out.flush();
			in = new ObjectInputStream(s.getInputStream());

			while (!close) {
				Object request = in.readObject();
				if (request instanceof Message) {
					switch (((Message) request).getCode()) {

					case CONNECT_ME:
						boolean added = master.addNode(s.getInetAddress(), s.getPort());
						if (added) {
							out.writeObject(new Message(ClusterProtocol.STATUS_REPORT));
							out.flush();
						} else {
							out.writeObject(new Message(ClusterProtocol.BYE));
							out.flush();
							close = true;
							s.close();
						}
						break;

					case TASK_REPORT:
						System.out.println("MASTER HANDLE: received a task report from worker ! Task seems to be done");
						// TODO alert master that task is now done or canceled
						// (according to the report)
						out.writeObject(new Message(ClusterProtocol.BYE));
						close = true;
						s.close();
						break;
					case BYE:
						close = true;
						s.close();
						break;
					default:
						out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
						s.close();
						close = true;
						break;
					}
				} else {
					out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
					s.close();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
