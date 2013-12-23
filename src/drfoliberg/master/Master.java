package drfoliberg.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import drfoliberg.Status;
import drfoliberg.network.ClusterProtocol;
import drfoliberg.network.Message;
import drfoliberg.task.Task;

public class Master extends Thread {

	ListenerMaster listener;

	private ArrayList<Node> nodes;

	public Master() {
		this.nodes = new ArrayList<Node>();
		listener = new ListenerMaster(this);
		listener.start();
	}

	public boolean dispatch(Task task, Node node) {
		DispatcherMaster dispatcher = new DispatcherMaster(node, task);
		dispatcher.start();
		return false;
	}

	public boolean disconnectNode(Node n) {
		try {
			Socket s = new Socket(n.getNodeAddress(), n.getNodePort());
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			out.writeObject(new Message(ClusterProtocol.DISCONNECT_ME));
			out.flush();
			Object o = in.readObject();
			if (o instanceof Message) {
				Message m = (Message) o;
				switch (m.getCode()) {
				case ClusterProtocol.BYE:
					System.out.println("MASTER: node removed!");
					removeNode(n.getNodeAddress(), n.getNodePort());
					s.close();
					break;

				default:
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean addNode(InetAddress address, int port) {
		Node n = new Node(address, port);
		if (nodes.contains(n)) {
			System.out.println("MASTER: Could not add node!");
			return false;
		} else {
			n.setStatus(Status.FREE);
			nodes.add(n);
			System.out.println("MASTER: Added new node to node list!");
			return true;
		}
	}

	public boolean removeNode(InetAddress address, int port) {
		Node n = new Node(address, port);
		if (nodes.remove(n)) {
			System.out.println("NODE REMOVED");
			return true;
		}
		return false;
	}

	public void run() {
		while (true) {
			// TODO move to node checker
			try {
				if (nodes.size() > 0) {
					System.out.println("MASTER: checking if nodes are still alive");
					for (Node n : nodes) {
						System.out.println("checking node: " + n.getNodeAddress().toString());
						//TODO check if node is alive (ask for status report)
						//update the node list and task list
					}
				} else {
					System.out.println("MASTER: no nodes to check!");
				}
				System.out.println("MASTER: checking back in 30 seconds");
				sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
