package drfoliberg.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import drfoliberg.common.Node;
import drfoliberg.common.Status;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.Message;
import drfoliberg.common.network.StatusReport;
import drfoliberg.common.network.TaskReport;
import drfoliberg.common.task.Job;
import drfoliberg.common.task.Task;
import drfoliberg.master.listeners.IMasterListener;
import drfoliberg.master.listeners.INodeListener;
import drfoliberg.master.listeners.ITaskListener;

public class Master extends Thread implements INodeListener, ITaskListener, IMasterListener {

	MasterServer listener;
	NodeChecker nodeChecker;

	private ArrayList<Node> nodes;
	public ArrayList<Job> jobs;
	private HashMap<String, Node> nodesByUNID;

	public Master() {
		this.nodes = new ArrayList<Node>();
		this.jobs = new ArrayList<Job>();
		this.nodesByUNID = new HashMap<>();
		this.listener = new MasterServer(this);
		this.nodeChecker = new NodeChecker(this);

	}

	public synchronized Node identifySender(String nodeId) {
		Node n = this.nodesByUNID.get(nodeId);
		if (n == null) {
			System.out.println("WARNING could not FIND NODE " + nodeId + " Size of nodesByUNID: " + nodesByUNID.size()
					+ " Size of nodes arraylist:" + nodes.size());
		}
		return n;
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

	private synchronized boolean updateNodesWork() {
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

	public synchronized boolean dispatch(Task task, Node node) {
		DispatcherMaster dispatcher = new DispatcherMaster(node, task, this);
		dispatcher.start();
		return true;
	}

	public void nodeShutdown(Node n) {
		Node sender = identifySender(n.getUnid());
		if (sender != null) {

			// Cancel node's task status if any
			Task toCancel = null;
			toCancel = sender.getCurrentTask();
			if (toCancel != null) {
				updateNodeTask(sender, Status.JOB_TODO);
			}

			// Update node status
			removeNode(sender);

		} else {
			System.err.println("Could not mark node as disconnected as it was not found");
		}
	}

	public synchronized boolean disconnectNode(Node n) {
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

	private synchronized String getNewUNID(Node n) {
		System.out.println("MASTER: generating a nuid for node " + n.getName());
		long ms = System.currentTimeMillis();
		String input = ms + n.getName();
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] byteArray = md.digest(input.getBytes());
		String result = "";
		for (int i = 0; i < byteArray.length; i++) {
			result += Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1);
		}
		System.out.println("MASTER: generated " + result + " for node " + n.getName());
		return result;
	}

	public synchronized boolean addNode(Node n) {
		if (nodes.contains(n)) {
			System.out.println("MASTER: Could not add node!");
			return false;
		} else {
			n.setStatus(Status.NOT_CONNECTED);
			String unid = getNewUNID(n);
			n.setUnid(unid);
			nodes.add(n);
			nodesByUNID.put(n.getUnid(), n);
			System.out.println("MASTER: Added node " + n.getName() + " with unid: " + n.getUnid());
			updateNodesWork();
			return true;
		}
	}

	public synchronized ArrayList<Node> getNodes() {
		return this.nodes;
	}

	public synchronized boolean updateNodeTask(Node n, Status updateStatus) {
		Task task = n.getCurrentTask();

		if (task != null) {
			System.out.println("MASTER: the task " + n.getCurrentTask().getTaskId() + " is now " + updateStatus);
			task.setStatus(updateStatus);
			if (updateStatus == Status.JOB_COMPLETED) {
				n.getCurrentTask().setStatus(Status.JOB_COMPLETED);
				// n.setStatus(Status.FREE); Node now updates it's own status
				// and sends back to master
				n.setCurrentTask(null);
			} else {
				// TODO
			}
			updateNodesWork();
		} else {
			System.out.println("MASTER: no task was found for node " + n.getName());
		}
		return false;
	}

	public synchronized void readStatusReport(StatusReport report) {
		Status s = report.getNode().getStatus();
		Node sender = identifySender(report.getNode().getUnid());
		sender.setStatus(s);
		updateNodesWork();
	}

	public synchronized boolean readTaskReport(TaskReport report) {

		double progress = report.getProgress();
		// find node
		report.getJobId();
		report.getTaskId();
		Node sender = null;
		for (Node n : this.nodes) {
			if (n.getCurrentTask() != null && n.getCurrentTask().getJobId() == report.getJobId()) {
				sender = n;
				break;
			}
		}
		if (sender == null) {
			System.out.println("MASTER: Could not find task in the node list!");
			return false;
		}
		System.out
				.println("MASTER: Updating the task " + sender.getCurrentTask().getTaskId() + " to " + progress + "%");
		sender.getCurrentTask().setProgress(progress);
		if (progress == 100) {
			updateNodeTask(sender, Status.JOB_COMPLETED);
		}
		return true;
	}

	public synchronized boolean removeNode(Node n) {
		// updateNodeTask(n, Status.JOB_TODO);
		this.nodesByUNID.remove(n);
		if (nodes.remove(n)) {
			System.out.println("NODE REMOVED");
			return true;
		}
		return false;
	}

	public void run() {
		// TODO read configuration from previous run and start services

		// start services
		this.listener.start();
		this.nodeChecker.start();
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

	@Override
	public void workUpdated() {
		// TODO Auto-generated method stub

	}
}
