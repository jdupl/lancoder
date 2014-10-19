package org.lancoder.worker.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.messages.TaskRequestMessage;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.pool.Pooler;

public class WorkerHandler extends Pooler<Socket> {

	private WorkerServerListener listener;

	public WorkerHandler(WorkerServerListener listener) {
		this.listener = listener;
	}

	@Override
	protected void start() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(task.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(task.getInputStream());
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
			if (task != null && !task.isClosed()) {
				try {
					task.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void serviceFailure(Exception e) {

	}
}
