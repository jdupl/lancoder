package org.lancoder.master;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.lancoder.common.Node;
import org.lancoder.common.events.Event;
import org.lancoder.common.events.EventEnum;
import org.lancoder.common.events.EventListener;
import org.lancoder.common.network.cluster.messages.ConnectRequest;
import org.lancoder.common.network.cluster.messages.ConnectResponse;
import org.lancoder.common.status.NodeState;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.master.impl.Master;

public class NodeManager implements EventListener {

	private final static int FAILURE_THRESHOLD = 10;

	private EventListener listener;
	private MasterConfig masterConfig;
	private final HashMap<String, Node> nodes = new HashMap<>();

	public NodeManager(EventListener listener, MasterConfig masterConfig, MasterSavedInstance instance) {
		this.listener = listener;
		this.masterConfig = masterConfig;

		if (instance != null) {
			nodes.putAll(instance.getNodes());
			for (Node node : getNodes()) { // Reset statuses
				node.unlock();
				node.setStatus(NodeState.NOT_CONNECTED);
			}
		}
	}

	public HashMap<String, Node> getNodeHashMap() {
		return this.nodes;
	}

	/**
	 * Returns a node object from a node id
	 *
	 * @param nodeId
	 *            The node ID to get
	 * @return The node object or null if not found
	 */
	public Node identifySender(String nodeId) {
		Node n = this.nodes.get(nodeId);

		if (n == null) {
			Logger logger = Logger.getLogger("lancoder");
			logger.warning(String.format("Could not find node %s%n", nodeId));
		}
		return n;
	}

	public synchronized ArrayList<Node> getNodes() {
		ArrayList<Node> nodes = new ArrayList<>();
		for (Entry<String, Node> e : this.nodes.entrySet()) {
			nodes.add(e.getValue());
		}
		return nodes;
	}

	public synchronized ArrayList<Node> getOnlineNodes() {
		ArrayList<Node> nodes = new ArrayList<>();
		for (Entry<String, Node> e : this.nodes.entrySet()) {
			Node n = e.getValue();
			if (n.getStatus() != NodeState.PAUSED && n.getStatus() != NodeState.NOT_CONNECTED) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	public synchronized ArrayList<Node> getFreeNodes() {
		ArrayList<Node> nodes = new ArrayList<>();

		for (Node node : this.getOnlineNodes()) {
			if (!node.isLocked() && isAvailable(node)) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	/**
	 * Check if a node is available for work.
	 *
	 * @param node
	 * @return
	 */
	public <T extends ClientTask> boolean isAvailable(Node node) {
		// TODO allow dynamic failure threshold
		boolean available = node.getFailureCount() < FAILURE_THRESHOLD
				&& node.getAllTasks().size() < node.getThreadCount();

		if (!available) {
			return false;
		}

		for (ClientTask task : node.getCurrentTasks()) {
			if (task instanceof ClientVideoTask) {
				available = false;
				break;
			}
		}

		return available;
	}

	/**
	 * Adds a node to the node list. Assigns a new ID to the node if it's non-existent. The node will be picked up by
	 * the node checker automatically if work is available.
	 *
	 * @param n
	 *            The node to be added
	 * @return if the node could be added
	 */
	private synchronized boolean addNode(Node n) {
		boolean success = true;
		// Is this a new node ?
		if (n.getUnid() == null || n.getUnid().equals("")) {
			n.setUnid(getNewUNID(n));
		}
		Node masterInstance = nodes.get(n.getUnid());

		if (masterInstance != null && masterInstance.getStatus() == NodeState.NOT_CONNECTED) {
			// Node with same unid reconnecting
			masterInstance.setStatus(NodeState.FREE);

			Logger logger = Logger.getLogger("lancoder");
			logger.fine(String.format("Node %s with id %s reconnected.%n", n.getName(), n.getUnid()));
		} else if (masterInstance == null) {
			n.setStatus(NodeState.FREE);
			nodes.put(n.getUnid(), n);

			Logger logger = Logger.getLogger("lancoder");
			logger.fine(String.format("Added new node %s with id %s.%n", n.getName(), n.getUnid()));
		} else {
			success = false;
		}

		n.unlock(); // remove lock on the node
		if (success) {
			listener.handle(new Event(EventEnum.WORK_NEEDS_UPDATE));
		}
		return success;
	}

	/**
	 * Set disconnected status to node and cancel node's tasks. Use shutdownNode() to gracefully shutdown a node.
	 *
	 *
	 * @param n
	 *            The node to disconnect
	 */
	public synchronized void removeNode(Node n) {
		Logger logger = Logger.getLogger("lancoder");

		if (n != null) {
			logger.fine(String.format("Disconnecting node %s%n", n.getName()));
			n.setStatus(NodeState.NOT_CONNECTED);
			listener.handle(new Event(EventEnum.WORK_NEEDS_UPDATE));
		}
	}

	private String getNewUNID(Node n) {
		String algorithm = Master.ALGORITHM;
		String result = "";
		long ms = System.currentTimeMillis();
		String input = ms + n.getName();
		MessageDigest md = null;

		try {
			md = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			// print and handle exception
			// if a null string is given back to the client, it won't connect
			Logger logger = Logger.getLogger("lancoder");
			logger.severe(String.format("Could not get an instance of %s to produce a UNID.%n", algorithm));
			logger.severe(e.getMessage());

			return "";
		}
		byte[] byteArray = md.digest(input.getBytes());
		result = "";
		for (int i = 0; i < byteArray.length; i++) {
			result += Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public ConnectResponse connectRequest(ConnectRequest cm, InetAddress detectedIp) {
		String unid = null;
		Node sender = cm.getNode();
		sender.setNodeAddress(detectedIp);
		sender.setUnid(cm.getUnid());
		if (this.addNode(sender)) {
			unid = sender.getUnid();
		}
		return new ConnectResponse(unid, "http", masterConfig.getApiServerPort());
	}

	public void disconnectRequest(ConnectRequest cm) {
		Node n = this.identifySender(cm.getUnid());
		this.removeNode(n);
	}

	@Override
	public void handle(Event event) {
		switch (event.getCode()) {
		case NODE_DISCONNECTED:
			Node disconnectedNode = (Node) event.getObject();
			removeNode(disconnectedNode);
			break;
		default:
			break;
		}
	}

}
