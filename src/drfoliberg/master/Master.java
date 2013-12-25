package drfoliberg.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import drfoliberg.common.Status;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.Message;
import drfoliberg.common.task.Job;
import drfoliberg.common.task.Task;
import drfoliberg.master.listeners.INodeListener;
import drfoliberg.master.listeners.ITaskListener;

public class Master extends Thread implements INodeListener, ITaskListener {

	MasterServer listener;

	private ArrayList<Node> nodes;
	public ArrayList<Job> jobs;

	private HashMap<Task, Job> processingTasks;

	public Master() {
		this.nodes = new ArrayList<Node>();
		this.jobs = new ArrayList<Job>();
		this.processingTasks = new HashMap<>();
		listener = new MasterServer(this);
		listener.start();
	}

	public synchronized boolean addJob(Job j) {
		boolean success = this.jobs.add(j);
		if (!success) {
			return false;
		}
		updateNodesWork();
		return success;
	}

	private Task getNextTask() {
		for (Job j : jobs) {
			ArrayList<Task> tasks = j.getTasks();
			for (Task task : tasks) {
				if (task.getStatus() == Status.JOB_TODO) {
					return task;
				}
			}
		}
		return null;
	}

	private Node getBestFreeNode() {
		for (Node n : this.nodes) {
			if (n.getStatus() == Status.FREE) {
				return n;
			}
		}
		return null;
	}

	private boolean updateNodesWork() {
		System.out.println("UPDATING WORK");
		Task nextTask = getNextTask();
		if (nextTask == null) {
			System.out.println("MASTER: No available work!");
			return false;
		}
		Node node = getBestFreeNode();
		if (node == null) {
			System.out.println("MASTER: No available nodes!");
			return false;
		}
		dispatch(nextTask, node);

		return true;
	}

	public boolean dispatch(Task task, Node node) {
		DispatcherMaster dispatcher = new DispatcherMaster(node, task);
		dispatcher.start();
		return true;
	}

	public boolean disconnectNode(Node n) {
		try {
			updateNodeTask(n, Status.JOB_TODO);
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
				case BYE:
					removeNode(n);
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

	public synchronized boolean addNode(Node n) {
		// Node n = new Node(address, port, ("Worker" + this.nodes.size()));
		if (nodes.contains(n)) {
			System.out.println("MASTER: Could not add node!");
			return false;
		} else {
			n.setStatus(Status.FREE);
			nodes.add(n);
			System.out.println("MASTER: Added new node " + n.getName() + "to node list!");
			updateNodesWork();
			return true;
		}
	}

	public synchronized ArrayList<Node> getNodes() {
		return this.nodes;
	}
	
	public boolean updateNodeTask(Node n, Status updateStatus) {
		Task cancelledTask = n.getCurrentTask();
		if (cancelledTask != null) {
			System.out.println("Cancelling the task of the node " + n.getName());
			processingTasks.get(cancelledTask).getTasks().get(cancelledTask.getTaskId()).setStatus(Status.JOB_TODO);
			updateNodesWork();
		}
		return false;
	}

	public synchronized boolean removeNode(Node n) {
		updateNodeTask(n, Status.JOB_TODO);
		if (nodes.remove(n)) {
			System.out.println("NODE REMOVED");
			return true;
		}
		return false;
	}

	public void run() {

	}

	@Override
	public void nodeAdded(Node n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nodeDisconnected(Node n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nodeRemoved(Node n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void taskFinished(Task t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void taskCancelled(Task t) {
		// TODO Auto-generated method stub
		
	}
}
