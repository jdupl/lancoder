package drfoliberg.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

import drfoliberg.common.Node;
import drfoliberg.common.Status;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.Message;
import drfoliberg.common.network.TaskRequestMessage;
import drfoliberg.common.task.Task;

public class DispatcherMaster implements Runnable {

	Node node;
	Task task;
	Master master;

	public DispatcherMaster(Node node, Task task, Master master) {
		this.node = node;
		this.task = task;
	}

	public void run() {
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
					node.setCurrentTask(task);
					task.setStatus(Status.JOB_COMPUTING);
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
		} catch (ConnectException e) {
			// TODO: handle node not listening ?
			System.out.println("MASTER HANDLE: could not send packet to worker! WORKER IS OFFLINE");

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

}
