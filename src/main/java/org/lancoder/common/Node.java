package org.lancoder.common;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

import org.lancoder.common.status.NodeState;
import org.lancoder.common.task.Task;

public class Node implements Serializable {

	private static final long serialVersionUID = 3450445684775221368L;
	private InetAddress nodeAddress;
	private int nodePort;
	private NodeState status;
	private String name;
	private String unid;
	private ArrayList<Task> currentTasks = new ArrayList<>();

	public Node(InetAddress nodeAddresse, int nodePort, String name) {
		this.nodeAddress = nodeAddresse;
		this.nodePort = nodePort;
		this.name = name;
		this.status = NodeState.NOT_CONNECTED;
	}

	public boolean hasTask(Task task) {
		for (Task nodeTask : this.getCurrentTasks()) {
			if (nodeTask.equals(task)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "Node [nodeAddress=" + nodeAddress + ", nodePort=" + nodePort + ", status=" + status + ", name=" + name
				+ ", unid=" + unid + ", currentTask=" + currentTasks.toArray() + "]";
	}

	public boolean equals(Object o) {
		boolean equals = false;
		if (o instanceof Node) {
			Node n = (Node) o;
			if (n.getNodeAddress().equals(this.getNodeAddress()) && n.getNodePort() == this.getNodePort()) {
				equals = true;
			}
		}
		return equals;
	}

	public InetAddress getNodeAddress() {
		return nodeAddress;
	}

	public void setNodeAddress(InetAddress nodeAddress) {
		this.nodeAddress = nodeAddress;
	}

	public NodeState getStatus() {
		return status;
	}

	public void setStatus(NodeState status) {
		this.status = status;
	}

	public int getNodePort() {
		return nodePort;
	}

	public void setNodePort(int nodePort) {
		this.nodePort = nodePort;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Task> getCurrentTasks() {
		return currentTasks;
	}

	public void addTask(Task currentTask) {
		this.currentTasks.add(currentTask);
	}

	public String getUnid() {
		return unid;
	}

	public void setUnid(String nodeIdentifier) {
		this.unid = nodeIdentifier;
	}
}
