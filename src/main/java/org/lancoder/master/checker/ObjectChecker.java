package org.lancoder.master.checker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingDeque;

import org.lancoder.common.Node;
import org.lancoder.common.RunnableService;
import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;

public class ObjectChecker extends RunnableService implements Comparable<ObjectChecker> {

	private NodeCheckerListener listener;
	final LinkedBlockingDeque<Node> tasks = new LinkedBlockingDeque<Node>();

	public ObjectChecker(NodeCheckerListener listener) {
		this.listener = listener;
	}

	public synchronized boolean add(Node n) {
		return this.tasks.offer(n);
	}

	public int getQueueSize() {
		return this.tasks.size();
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
	public void run() {
		while (!close) {
			try {
				Node next = tasks.take();
				checkNode(next);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}

	@Override
	public int compareTo(ObjectChecker o) {
		return Integer.compare(getQueueSize(), o.getQueueSize());
	}
}
