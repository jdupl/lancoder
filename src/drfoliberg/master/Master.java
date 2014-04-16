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

/**
 * TODO implement a clean shutdown method (saving current config and such)
 */
public class Master implements Runnable {

	MasterServer listener;
	NodeChecker nodeChecker;
	
	public static final String ALGORITHM = "SHA-256";

	private ArrayList<Node> nodes;
	public ArrayList<Job> jobs;
	private HashMap<String, Node> nodesByUNID;

	public Master() {
		this.nodes = new ArrayList<Node>();
		this.jobs = new ArrayList<Job>();
		this.nodesByUNID = new HashMap<>();
        // TODO refactor these to observers/events patterns
		this.listener = new MasterServer(this);
		this.nodeChecker = new NodeChecker(this);
	}

	/**
	 * Returns a node object from a node id
	 * 
	 * @param nodeId
	 *            The node ID to get
	 * @return The node object or null if not found
	 */
	public synchronized Node identifySender(String nodeId) {
		Node n = this.nodesByUNID.get(nodeId);
		if (n == null) {
			System.out.printf("WARNING could not FIND NODE %s\n"
					+ "Size of nodesByUNID: %d\n"
					+ "Size of nodes arraylist:%d\n",
					nodeId, nodesByUNID.size(), nodes.size());
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

	/**
	 * This should look for available online and free nodes.
	 * TODO The node order should be intelligent. Fastest node should be selected.
	 * @return pointer to the node object
	 */
	private Node getBestFreeNode() {
		for (Node n : this.nodes) {
			if (n.getStatus() == Status.FREE) {
				return n;
			}
		}
		return null;
	}

	/**
	 * Checks if any task and nodes are available and dispatch until possible.
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
		return true;
	}	

	public synchronized boolean dispatch(Task task, Node node) {
		DispatcherMaster dispatcher = new DispatcherMaster(node, task, this);
		Thread t = new Thread(dispatcher);
		t.start();
		return true;
	}

	/**
	 * Handles a node's request to be disconnected.  
	 * @param unid The sender of the request (node to disconnect)
	 */
	public void nodeShutdown(String unid) {
		Node sender = identifySender(unid);
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

	/**
	 * Sends a disconnect request to a node, removes the node from the node list and 
	 * updates the task of the node if it had any.
	 * @param n The node to remove
	 * @return
	 */
	public synchronized boolean disconnectNode(Node n) {
		try {
			//TODO only update work if worker has a task
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
			System.out.println("MASTER: could not get an instance of " + ALGORITHM + " to produce a UNID\nThis is bad.");
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
	 * Adds a node to the node list.
	 * Assigns a new ID to the node if it's non-existent.
	 * The node will be picked up by the node checker automatically if work is available.
	 * @param n The node to be added
	 * @return if the node could be added
	 */
    public synchronized boolean addNode(Node n) {
        // Is this a new node ?
        if (n.getUnid().equals("")) {
            n.setUnid(getNewUNID(n));
        }
        if (nodes.contains(n)) {
            System.out.println("MASTER: Could not add node!");
            // TODO handle node with same unid reconnecting
            return false;
        } else {
            n.setStatus(Status.NOT_CONNECTED);
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

	/**
	 * Reads a status report of a node and updates the status of the node.
	 * 
	 * @param report
	 *            The report to be read
	 * @return true if update could be sent, false otherwise
	 */
	public synchronized boolean readStatusReport(StatusReport report) {
		Status s = report.status;
		String unid = report.getUnid();
		Node sender = identifySender(unid);
		// only update if status is changed
		if (sender.getStatus() != report.status) {
			System.out.println("node " + sender.getName()
					+ " is updating it's status from " + sender.getStatus()
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
     * @param report The report to be read
     * @return Return true if update could be sent, false otherwise
     */
    public synchronized boolean readTaskReport(TaskReport report) {

        double progress = report.getProgress();
        // find node
        String nodeId = report.getUnid();
        Node sender = identifySender(nodeId);

        if (sender == null) {
            System.err.println("MASTER: Could not find task in the node list!");
            return false;
        }
        
        Task nodeTask = sender.getCurrentTask();
        
        if (nodeTask == null) {
            System.err.printf("MASTER: Node %s has no task! \n",sender.getName());
            return false;
        }

        // check task-node association
        if (!nodeTask.getJobId().equals(report.getJobId()) || nodeTask.getTaskId() != report.getTaskId()) {
            System.err.printf("MASTER: Bad task update from node %s." +
                            " Expected task: %d, job: %s." +
                            " Got task: %d, job: %s", sender.getUnid(), nodeTask.getTaskId(),
                    nodeTask.getJobId(), report.getTaskId(), report.getJobId()
            );
            return false;
        }

        System.out.println("MASTER: Updating the task " + sender.getCurrentTask().getTaskId() + " to " + progress + "%");
        sender.getCurrentTask().setProgress(progress);
        if (progress == 100) {
            updateNodeTask(sender, Status.JOB_COMPLETED);
        }
        return true;
    }

	public synchronized boolean removeNode(Node n) {
		// updateNodeTask(n, Status.JOB_TODO);
        // TODO don't remove the node from the array list. Give it an offline status
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
		Thread listenerThread = new Thread(listener);
		listenerThread.start();
		Thread nodeCheckerThread = new Thread(nodeChecker);
		nodeCheckerThread.start();
	}

}
