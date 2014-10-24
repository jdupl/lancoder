package org.lancoder.master.dispatcher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.lancoder.common.Node;
import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.pool.Pooler;

public class Dispatcher extends Pooler<DispatchItem> {

	private DispatcherListener listener;

	public Dispatcher(DispatcherListener listener) {
		this.listener = listener;
	}

	private void dispatch(DispatchItem item) {
		Node node = item.getNode();
		System.out.println(node.getStatus());
		ClusterProtocol handled = null;
		try (Socket socket = new Socket()) {
			InetSocketAddress addr = new InetSocketAddress(node.getNodeAddress(), node.getNodePort());
			socket.connect(addr, 2000);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			out.writeObject(item.getMessage());
			out.flush();
			Object o = in.readObject();
			if (o instanceof Message) {
				Message m = (Message) o;
				handled = m.getCode();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			switch (handled) {
			case TASK_ACCEPTED:
				listener.taskAccepted(item);
				break;
			case TASK_REFUSED:
				listener.taskRefused(item);
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected void start() {
		dispatch(task);
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}

}
