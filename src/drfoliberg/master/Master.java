package drfoliberg.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import drfoliberg.common.Node;
import drfoliberg.common.Status;
import drfoliberg.common.job.Job;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.messages.CrashReport;
import drfoliberg.common.network.messages.Message;
import drfoliberg.common.network.messages.StatusReport;
import drfoliberg.common.task.Task;
import drfoliberg.common.task.TaskReport;
import drfoliberg.master.api.ApiServer;

/**
 * TODO implement a clean shutdown method (saving current config and such)
 */
public class Master implements Runnable {

	public static final String ALGORITHM = "SHA-256";

	private MasterServer listener;
	private NodeChecker nodeChecker;
	private HashMap<String, Node> nodes;

	private MasterConfig config;
	public ArrayList<Job> jobs; // change to private after tests

	private ApiServer apiServer;

	public Master() {
		nodes = new HashMap<String, Node>();
		jobs = new ArrayList<Job>();
		config = MasterConfig.load();

		// TODO refactor these to observers/events patterns
		listener = new MasterServer(this);
		nodeChecker = new NodeChecker(this);
		// api server to serve/get information from users
		apiServer = new ApiServer(this);
	}

	/**
	 * Returns a node object from a node id
	 * 
	 * @param nodeId
	 *            The node ID to get
	 * @return The node object or null if not found
	 */
	public synchronized Node identifySender(String nodeId) {
		Node n = this.nodes.get(nodeId);
		if (n == null) {
			System.out.printf("WARNING could not FIND NODE %s\n" + "Size of nodesByUNID: %d\n"
					+ "Size of nodes arraylist:%d\n", nodeId, nodes.size(), nodes.size());
		}
		return n;
	}

	public boolean addJob(Job j) {
		boolean success = this.jobs.add(j);
		if (!success) {
			return false;
		}
		updateNodesWork();
		config.dump();
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

	/**
	 * This should look for available online and free nodes. TODO The node order should be intelligent. Fastest node
	 * should be selected.
	 * 
	 * @return pointer to the node object
	 */
	private Node getBestFreeNode() {
		for (Entry<String, Node> entry : nodes.entrySet()) {
			Node n = entry.getValue();
			if (n.getStatus() == Status.FREE) {
				return n;
			}
		}
		return null;
	}

	/**
	 * Checks if any task and nodes are available and dispatch until possible.
	 * 
	 * @return true if any work was dispatched
	 */
	private synchronized boolean updateNodesWork() {
		// TODO loop to send more tasks (not just once)
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
		config.dump();
		return true;
	}

	public boolean dispatch(Task task, Node node) {
		DispatcherMaster dispatcher = new DispatcherMaster(node, task, this);
		Thread t = new Thread(dispatcher);
		t.start();
		return true;
	}

	private String getNewUNID(Node n) {
		String result = "";
		System.out.println("MASTER: generating a nuid for node " + n.getName());
		long ms = System.currentTimeMillis();
		String input = ms + n.getName();
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			// print and handle exception
			// if a null string is given back to the client, it won't connect
			e.printStackTrace();
			System.out
					.println("MASTER: could not get an instance of " + ALGORITHM + " to produce a UNID\nThis is bad.");
			return "";
		}
		byte[] byteArray = md.digest(input.getBytes());
		result = "";
		for (int i = 0; i < byteArray.length; i++) {
			result += Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1);
		}
		System.out.println("MASTER: generated " + result + " for node " + n.getName());
		return result;
	}

	/**
	 * Sends a disconnect request to a node, removes the node from the node list and updates the task of the node if it
	 * had any.
	 * 
	 * @param n
	 *            The node to remove
	 * @return Successfully found and removed the node
	 */
	public boolean disconnectNode(Node n) {
		try {
			// TODO only update work if worker has a task
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

	/**
	 * Adds a node to the node list. Assigns a new ID to the node if it's non-existent. The node will be picked up by
	 * the node checker automatically if work is available.
	 * 
	 * @param n
	 *            The node to be added
	 * @return if the node could be added
	 */
	public boolean addNode(Node n) {
		// Is this a new node ?
		if (n.getUnid().equals("")) {
			n.setUnid(getNewUNID(n));
		}
		if (nodes.containsKey(n.getUnid())) {
			System.out.println("MASTER: Could not add node!");
			// TODO handle node with same unid reconnecting -> offline status
			return false;
		} else {
			n.setStatus(Status.NOT_CONNECTED);
			nodes.put(n.getUnid(), n);
			System.out.println("MASTER: Added node " + n.getName() + " with unid: " + n.getUnid());
			updateNodesWork();
			return true;
		}
	}

	public ArrayList<Node> getNodes() {
		ArrayList<Node> nodes = new ArrayList<>();
		for (Entry<String, Node> e : this.nodes.entrySet()) {
			nodes.add(e.getValue());
		}
		return nodes;
	}

	/**
	 * Set disconnected status to node and cancel node's task
	 * 
	 * @param n
	 *            The node to disconnect
	 * @return
	 */
	public synchronized boolean removeNode(Node n) {
		if (n != null) {
			// Cancel node's task status if any
			Task toCancel = null;
			toCancel = n.getCurrentTask();
			if (toCancel != null) {
				updateNodeTask(n, Status.JOB_TODO);
			}
			n.setStatus(Status.NOT_CONNECTED);
		} else {
			System.err.println("Could not mark node as disconnected as it was not found");
		}
		return false;
	}

	public boolean updateNodeTask(Node n, Status updateStatus) {
		Task task = n.getCurrentTask();
		// TODO clean logic here
		if (task != null) {
			System.out.println("MASTER: the task " + n.getCurrentTask().getTaskId() + " is now " + updateStatus);
			task.setStatus(updateStatus);
			if (updateStatus == Status.JOB_COMPLETED) {
				n.getCurrentTask().setStatus(Status.JOB_COMPLETED);
				n.setCurrentTask(null);
			} else if (updateStatus == Status.JOB_CANCELED) {
				task.setProgress(0);
				task.setStatus(Status.JOB_TODO);
				n.setCurrentTask(null);
			}
			updateNodesWork();
		} else {
			System.err.println("MASTER: no task was found for node " + n.getName());
		}
		return false;
	}

	/**
	 * Reads a status report of a node and updates the status of the node.
	 * 
	 * @param report
	 *            The report to be read
	 * @return true if update could be sent, false otherwise
	 */
	public boolean readStatusReport(StatusReport report) {
		Status s = report.status;
		String unid = report.getUnid();
		Node sender = identifySender(unid);
		// only update if status is changed
		if (sender.getStatus() != report.status) {
			System.out.println("node " + sender.getName() + " is updating it's status from " + sender.getStatus()
					+ " to " + report.status);
			sender.setStatus(s);
			updateNodesWork();
		}
		// TODO: get real return value of the update
		return true;
	}

	/**
	 * Reads a task report and launches an update of the task status and progress
	 * 
	 * @param report
	 *            The report to be read
	 * @return Return true if update could be sent, false otherwise
	 */
	public boolean readTaskReport(TaskReport report) {

		float progress = report.getTask().getProgress();
		// find node
		String nodeId = report.getUnid();
		Node sender = identifySender(nodeId);

		if (sender == null) {
			System.err.println("MASTER: Could not find task in the node list!");
			return false;
		}

		Task nodeTask = sender.getCurrentTask();

		if (nodeTask == null) {
			System.err.printf("MASTER: Node %s has no task! \n", sender.getName());
			return false;
		}

		// check task-node association
		if (!nodeTask.getJobId().equals(report.getTask().getJobId()) || nodeTask.getTaskId() != report.getTask().getTaskId()) {
			System.err.printf("MASTER: Bad task update from node %s." + " Expected task: %d, job: %s."
					+ " Got task: %d, job: %s", sender.getUnid(), nodeTask.getTaskId(), nodeTask.getJobId(),
					report.getTask().getTaskId(), report.getTask().getJobId());
			return false;
		}

		System.out
				.println("MASTER: Updating the task " + sender.getCurrentTask().getTaskId() + " to " + progress + "%");
		sender.getCurrentTask().setProgress(progress);
		
		sender.getCurrentTask().setTaskStatus(report.getTask().getTaskStatus());
		
		if(sender.getCurrentTask().getStatus() == Status.JOB_COMPLETED){
			updateNodeTask(sender, Status.JOB_COMPLETED);
		}
		return true;
	}

	public void readCrashReport(CrashReport report) {
		// TODO handle non fatal crashes (worker side first)
		// after a non-fatal crash, master should try X times to reassign tasks
		// from same job. After a fatal crash, leave the node connected but do
		// not assign anything to the node.
		// This way, node can reconnected if fatal crash is fixed.
		Node node = identifySender(report.getUnid());
		if (report.getCause().isFatal()) {
			System.err.printf("Node '%s' fatally crashed.\n", node.getName());
		} else {
			System.out.printf("Node %s crashed but not fatally.\n", node.getName());
		}
		updateNodeTask(node, Status.JOB_CANCELED);
	}

	public void run() {
		// start services
		Thread listenerThread = new Thread(listener);
		listenerThread.start();
		Thread nodeCheckerThread = new Thread(nodeChecker);
		nodeCheckerThread.start();
		Thread apiThread = new Thread(apiServer);
		apiThread.start();
	}

}
