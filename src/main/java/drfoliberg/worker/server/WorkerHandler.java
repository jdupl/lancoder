package drfoliberg.worker.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import drfoliberg.common.network.messages.ClusterProtocol;
import drfoliberg.common.network.messages.cluster.Message;
import drfoliberg.common.network.messages.cluster.TaskRequestMessage;

public class WorkerHandler implements Runnable {

	private WorkerServerListener listener;
	private Socket s;

	public WorkerHandler(Socket s, WorkerServerListener listener) {
		this.s = s;
		this.listener = listener;
	}

	@Override
	public void run() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			while (!s.isClosed()) {
				Object request = in.readObject();
				if (request instanceof Message) {
					Message requestMessage = (Message) request;
					switch (requestMessage.getCode()) {
					case TASK_REQUEST:
						if (requestMessage instanceof TaskRequestMessage) {
							TaskRequestMessage trm = (TaskRequestMessage) requestMessage;
							Message response = null;
							if (listener.taskRequest(trm.getTask())) {
								response = new Message(ClusterProtocol.TASK_ACCEPTED);
							} else {
								response = new Message(ClusterProtocol.TASK_REFUSED);
							}
							out.writeObject(response);
							out.flush();
							s.close();
						}
						break;
					case STATUS_REQUEST:
						out.writeObject(listener.statusRequest());
						out.flush();
						s.close();
						break;
					default:
						out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
						out.flush();
						s.close();
						break;
					}
				} else {
					out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
					out.flush();
					s.close();
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (s != null && !s.isClosed()) {
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
