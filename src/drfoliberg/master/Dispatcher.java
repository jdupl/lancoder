package drfoliberg.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import drfoliberg.common.Node;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.messages.cluster.Message;
import drfoliberg.common.network.messages.cluster.TaskRequestMessage;
import drfoliberg.common.status.TaskState;
import drfoliberg.common.task.Task;

public class Dispatcher implements Runnable {

	Node node;
	Task task;
	Master master;

	public Dispatcher(Node node, Task task, Master master) {
		this.node = node;
		this.task = task;
		this.master = master;
	}

	private void taskRefused() {
		task.setStatus(TaskState.TASK_TODO);
		master.updateNodesWork();
	}

	private void taskAccepted() {
		node.setCurrentTask(task);
		task.setStatus(TaskState.TASK_COMPUTING);
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
				taskRefused();
			} else {
				taskAccepted();
			}
		}
	}
}
