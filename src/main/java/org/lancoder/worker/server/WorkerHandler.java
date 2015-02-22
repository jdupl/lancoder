package org.lancoder.worker.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.messages.TaskRequestMessage;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.pool.PoolWorker;

public class WorkerHandler extends PoolWorker<Socket> {

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
//  			Object obj = new Message(ClusterProtocol.BAD_REQUEST);
			if (request instanceof Message) {
				Message requestMessage = (Message) request;
				switch (requestMessage.getCode()) {
				case DISCONNECT_ME:
					listener.shutdownWorker();
					obj = new Message(ClusterProtocol.OK);
					break;
				case TASK_REQUEST:
					if (requestMessage instanceof TaskRequestMessage) {
						TaskRequestMessage trm = (TaskRequestMessage) requestMessage;
						listener.taskRequest(trm.getTask());
    obj = new Message(ClusterProtocol.OK);
					}
					break;
				case UNASSIGN_TASK:
           if (requestMessage instanceof TaskRequestMessage) {
						TaskRequestMessage trm = (TaskRequestMessage) requestMessage;
						listener.deleteTask(trm.getTask());
						obj = new Message(ClusterProtocol.OK);
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
