package drfoliberg.common.network;

import java.io.Serializable;

import drfoliberg.common.task.Task;
import drfoliberg.master.Node;

public class Message implements Serializable {

	private static final long serialVersionUID = -483657531000641905L;

	private ClusterProtocol code;
	private Task task;
	private Node node;

	public Message(ClusterProtocol code) {
		this.code = code;
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
}
