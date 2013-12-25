package drfoliberg.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import drfoliberg.common.Status;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.Message;
import drfoliberg.common.task.Task;

public class DispatcherMaster extends Thread{

	Node node;
	Task task;
	
	public DispatcherMaster(Node node, Task task){
		this.node = node;
		this.task = task;
	}
	
	public void run(){
		try {
			Socket s = new Socket(node.getNodeAddress(), node.getNodePort());
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			System.out.println("MASTER DISPATCH: Sending work to node " + node.getName());
			out.writeObject(new Message(task));
			out.flush();
			System.out.println("MASTER DISPATCH: Waiting for node response");
			Object o = in.readObject();
			if (o instanceof Message) {
				Message m = (Message) o;
				switch (m.getCode()) {
				case TASK_REFUSED:
					System.out.println("MASTER DISPATCH: node seems to have refused the work!");
					out.writeObject(new Message(ClusterProtocol.BYE));
					out.flush();
					s.close();
					break;
				case TASK_ACCEPTED:
					System.out.println("MASTER DISPACTH: received that node accepted the task !");
					node.setStatus(Status.WORKING);
					out.writeObject(new Message(ClusterProtocol.BYE));
					out.flush();
					s.close();
					break;
				default:
					out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
					out.flush();
					s.close();
					System.out.println("MASTER DISPATCH: received invalid request");
					break;
				}
			}else{
				System.out.println("MASTER DISPATCH: received invalid message!!");
			}
			System.out.println("MASTER DISPATCH: closing dispatcher");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
