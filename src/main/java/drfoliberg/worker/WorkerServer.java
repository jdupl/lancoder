package main.java.drfoliberg.worker;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import main.java.drfoliberg.common.Service;
import main.java.drfoliberg.common.network.ClusterProtocol;
import main.java.drfoliberg.common.network.messages.cluster.Message;
import main.java.drfoliberg.common.network.messages.cluster.TaskRequestMessage;
import main.java.drfoliberg.common.status.NodeState;
import main.java.drfoliberg.common.status.TaskState;

public class WorkerServer extends Service {

	private Worker worker;

	public WorkerServer(Worker w) {
		this.worker = w;
	}

	public void print(String s) {
		System.out.println(worker.getWorkerName().toUpperCase() + " SERVER: " + s);
	}

	public void run() {
		try {
			ServerSocket server = new ServerSocket(worker.getListenPort());
			server.setSoTimeout(2000);
			while (!close) {
				try {
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
							worker.updateStatus(NodeState.NOT_CONNECTED);
							break;
						case TASK_REQUEST:
							print("received task from master");
							if (!(m instanceof TaskRequestMessage)) {
								out.writeObject(new Message(ClusterProtocol.TASK_REFUSED));
								out.flush();
							} else {
								TaskRequestMessage tqm = (TaskRequestMessage) m;
								if (tqm.task.getStatus() == TaskState.TASK_CANCELED) {
									out.writeObject(new Message(ClusterProtocol.TASK_ACCEPTED));
									out.flush();
									System.err.println("WORKER: master requests to stop work !");
									worker.stopWork(tqm.task);
								} else if (worker.startWork(tqm.task)) {
									out.writeObject(new Message(ClusterProtocol.TASK_ACCEPTED));
									out.flush();
									worker.updateStatus(NodeState.WORKING);
								} else {
									out.writeObject(new Message(ClusterProtocol.TASK_REFUSED));
									out.flush();
								}
							}
							s.close();
							break;
						case STATUS_REQUEST:
							out.writeObject(this.worker.getStatusReport());
							out.flush();
							s.close();
							break;
						case BYE:
							System.err.println("WORKER is closing socket");
							s.close();
							break;
						default:
							out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
							out.flush();
							s.close();
							break;
						}
					} else {
						print("could not read packet !");
					}
				} catch (InterruptedIOException e) {
					// interrupting every 2 seconds
				}
			}
			// closing socket
			System.out.println("Closing worker server !");
			if (server != null) {
				server.close();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
