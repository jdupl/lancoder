package org.lancoder.master.dispatcher;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.eclipse.jetty.util.BlockingArrayQueue;
import org.lancoder.common.Node;
import org.lancoder.common.RunnableService;
import org.lancoder.common.network.messages.ClusterProtocol;
import org.lancoder.common.network.messages.cluster.Message;
import org.lancoder.common.network.messages.cluster.TaskRequestMessage;
import org.lancoder.common.task.ClientTask;

public class ObjectDispatcher extends RunnableService {

	private DispatcherListener listener;
	private BlockingArrayQueue<DispatchItem> items = new BlockingArrayQueue<>();
	private boolean free = true;

	public ObjectDispatcher(DispatcherListener listener) {
		this.listener = listener;
	}

	public boolean isFree() {
		return free;
	}

	public void queue(DispatchItem item) {
		this.items.add(item);
	}

	private void dispatch(DispatchItem item) {
		free = false;
		ClientTask task = item.getTask();
		Node node = item.getNode();

		TaskRequestMessage trm = new TaskRequestMessage(task);
		boolean handled = false;

		Socket socket = null;
		try {
			socket = new Socket(node.getNodeAddress(), node.getNodePort());
			socket.setSoTimeout(2000);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			out.writeObject(trm);
			out.flush();

			Object o = in.readObject();
			if (o instanceof Message) {
				Message m = (Message) o;
				if (m.getCode() == ClusterProtocol.TASK_ACCEPTED) {
					listener.taskAccepted(item);
					handled = true;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (!handled) {
				listener.taskRefused(item);
			}
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		free = true;
	}

	@Override
	public void run() {
		while (!close) {
			try {
				dispatch(items.take());
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		// TODO Auto-generated method stub

	}

}
