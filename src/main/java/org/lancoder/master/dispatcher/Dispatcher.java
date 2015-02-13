package org.lancoder.master.dispatcher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.lancoder.common.Node;
import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.pool.PoolWorker;

public class Dispatcher extends PoolWorker<DispatchItem> {

	private DispatcherListener listener;

	public Dispatcher(DispatcherListener listener) {
		this.listener = listener;
	}

	private void dispatch(DispatchItem item) {
		Node node = item.getNode();
		ClusterProtocol returnCode = ClusterProtocol.OK;
		boolean success = false;
		try (Socket socket = new Socket()) {
			InetSocketAddress addr = new InetSocketAddress(node.getNodeAddress(), node.getNodePort());
			socket.setSoTimeout(2000);
			socket.connect(addr, 2000);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			out.writeObject(item.getMessage());
			out.flush();
			Object o = in.readObject();
			if (o instanceof Message) {
				Message m = (Message) o;
				returnCode = m.getCode();
				success = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (success && returnCode == ClusterProtocol.OK) {
				// dispatching was successful
				listener.taskAccepted(item);
			} else {
				// timeout or internal error
				listener.taskRefused(item);
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
