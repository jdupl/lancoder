package drfoliberg.common;

import java.io.Serializable;
import java.net.InetAddress;

import drfoliberg.common.task.Task;

public class Node implements Serializable {

	private static final long serialVersionUID = 3450445684775221368L;
	private InetAddress nodeAddress;
	private int nodePort;
	private Status status;
	private String name;
	private String unid;

	private Task currentTask;

	public Node(InetAddress nodeAddresse, int nodePort, String name) {
		this.nodeAddress = nodeAddresse;
		this.status = Status.NOT_CONNECTED;
		this.nodePort = nodePort;
		this.name = name;
	}
	
	

	@Override
	public String toString() {
		return "Node [nodeAddress=" + nodeAddress + ", nodePort=" + nodePort
				+ ", status=" + status + ", name=" + name + ", unid=" + unid
				+ ", currentTask=" + currentTask + "]";
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
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

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}

	public String getUnid() {
		return unid;
	}

	public void setUnid(String nodeIdentifier) {
		this.unid = nodeIdentifier;
	}
}
