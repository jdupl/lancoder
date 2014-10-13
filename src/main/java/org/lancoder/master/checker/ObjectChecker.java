package org.lancoder.master.checker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.lancoder.common.Node;
import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.pool.Pooler;

public class ObjectChecker extends Pooler<Node> {

	private NodeCheckerListener listener;

	public ObjectChecker(NodeCheckerListener listener) {
		this.listener = listener;
	}

	public void checkNode(Node n) {
		try (Socket s = new Socket()) {
			SocketAddress sockAddr = new InetSocketAddress(n.getNodeAddress(), n.getNodePort());
			s.connect(sockAddr, 2000);
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			Message m = new Message(ClusterProtocol.STATUS_REQUEST);
			out.writeObject(m);
			out.flush();
			Object o = in.readObject();
			if (o instanceof StatusReport) {
				listener.readStatusReport((StatusReport) o);
			}
		} catch (IOException e) {
			listener.nodeDisconnected(n);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}

	@Override
	protected void start() {
		checkNode(task);
	}
}
