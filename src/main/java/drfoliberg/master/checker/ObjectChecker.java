package drfoliberg.master.checker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import org.eclipse.jetty.util.BlockingArrayQueue;

import drfoliberg.common.Node;
import drfoliberg.common.RunnableService;
import drfoliberg.common.network.messages.ClusterProtocol;
import drfoliberg.common.network.messages.cluster.Message;
import drfoliberg.common.network.messages.cluster.StatusReport;

public class ObjectChecker extends RunnableService implements Comparable<ObjectChecker> {

	private NodeCheckerListener listener;
	final BlockingQueue<Node> tasks = new BlockingArrayQueue<Node>(100);

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
		try (Socket s = new Socket(n.getNodeAddress(), n.getNodePort())) {
			s.setSoTimeout(2000);
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
			// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub
	}

	@Override
	public int compareTo(ObjectChecker o) {
		return Integer.compare(getQueueSize(), o.getQueueSize());
	}
}
