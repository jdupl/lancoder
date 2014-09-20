package org.lancoder.worker.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.messages.TaskRequestMessage;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;

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
			Object request = in.readObject();
			Object obj = new Message(ClusterProtocol.BAD_REQUEST);
			if (request instanceof Message) {
				Message requestMessage = (Message) request;
				switch (requestMessage.getCode()) {
				case TASK_REQUEST:
					if (requestMessage instanceof TaskRequestMessage) {
						TaskRequestMessage trm = (TaskRequestMessage) requestMessage;
						if (listener.taskRequest(trm.getTask())) {
							obj = new Message(ClusterProtocol.TASK_ACCEPTED);
						} else {
							obj = new Message(ClusterProtocol.TASK_REFUSED);
						}
					}
					break;
				case STATUS_REQUEST:
					obj = listener.statusRequest();
					break;
				default:
					break;
				}
			}
			out.writeObject(obj);
			out.flush();
		} catch (Exception e) {
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
