package drfoliberg.common.network;

import java.io.Serializable;

import drfoliberg.common.Node;
import drfoliberg.common.task.Task;

public class Message implements Serializable {

	private static final long serialVersionUID = -483657531000641905L;

	protected ClusterProtocol code;
	private Task task;
	protected Node node;

	public Message(ClusterProtocol code) {
		this.code = code;
	}

	public Message(String nuid) {

	}

	public Message(Task t) {
		this.code = ClusterProtocol.TASK_REQUEST;
		this.task = t;
	}

	public Message(Node n) {
		this.code = ClusterProtocol.CONNECT_ME;
		this.node = n;
	}

	public ClusterProtocol getCode() {
		return code;
	}

	public Task getTask() {
		return task;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node n) {
		this.node = n;
	}
}
