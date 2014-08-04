package drfoliberg.common;

import java.io.Serializable;
import java.net.InetAddress;

import drfoliberg.common.status.NodeState;
import drfoliberg.common.task.video.VideoEncodingTask;

public class Node implements Serializable {

	private static final long serialVersionUID = 3450445684775221368L;
	private InetAddress nodeAddress;
	private int nodePort;
	private NodeState status;
	private String name;
	private String unid;

	private VideoEncodingTask currentTask;

	public Node(InetAddress nodeAddresse, int nodePort, String name) {
		this.nodeAddress = nodeAddresse;
		this.status = NodeState.NOT_CONNECTED;
		this.nodePort = nodePort;
		this.name = name;
	}

	@Override
	public String toString() {
		return "Node [nodeAddress=" + nodeAddress + ", nodePort=" + nodePort + ", status=" + status + ", name=" + name
				+ ", unid=" + unid + ", currentTask=" + currentTask + "]";
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

	public VideoEncodingTask getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(VideoEncodingTask currentTask) {
		// TODO move this
		if (currentTask == null) {
			System.out.printf("MASTER: %s now has NO TASK\n", name);
		} else {
			System.out.printf("MASTER: %s now has task id %d\n", name, currentTask.getTaskId());
		}

		this.currentTask = currentTask;
	}

	public String getUnid() {
		return unid;
	}

	public void setUnid(String nodeIdentifier) {
		this.unid = nodeIdentifier;
	}
}
