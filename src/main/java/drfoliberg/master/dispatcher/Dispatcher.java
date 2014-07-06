package main.java.drfoliberg.master.dispatcher;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import main.java.drfoliberg.common.Node;
import main.java.drfoliberg.common.network.ClusterProtocol;
import main.java.drfoliberg.common.network.messages.cluster.Message;
import main.java.drfoliberg.common.network.messages.cluster.TaskRequestMessage;
import main.java.drfoliberg.common.task.video.VideoEncodingTask;

@Deprecated
public class Dispatcher implements Runnable, DispatcherListener {

	Node node;
	VideoEncodingTask task;
	ArrayList<DispatcherListener> listeners;

	public Dispatcher(Node node, VideoEncodingTask task, DispatcherListener mainListener) {
		this.node = node;
		this.task = task;
		listeners = new ArrayList<>();
		listeners.add(mainListener);
	}

	public void taskRefused(VideoEncodingTask t, Node n) {
		for (DispatcherListener listener : listeners) {
			listener.taskRefused(t, n);
		}
	}

	public void taskAccepted(VideoEncodingTask t, Node n) {
		for (DispatcherListener listener : listeners) {
			listener.taskAccepted(t, n);
		}
	}

	public void run() {
		boolean success = false;
		try {
			Socket s = new Socket(node.getNodeAddress(), node.getNodePort());
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			System.out.println("MASTER DISPATCH: Sending work " + task.getTaskId() + " to node " + node.getName());
			out.writeObject(new TaskRequestMessage(task));
			out.flush();
			Object o = in.readObject();
			if (o instanceof Message) {
				Message m = (Message) o;
				switch (m.getCode()) {
				case TASK_REFUSED:
					System.err.println("MASTER DISPATCH: node refused task");
					s.close();
					break;
				case TASK_ACCEPTED:
					System.err.println("MASTER DISPATCH: node accepted task");
					success = true;
					s.close();
					break;
				default:
					out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
					out.flush();
					s.close();
					System.err.println("MASTER DISPATCH: received invalid request");
					break;
				}
			} else {
				System.out.println("MASTER DISPATCH: received invalid message!!");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (!success) {
				taskRefused(task, node);
			} else {
				taskAccepted(task, node);
			}
		}
	}
}
