package drfoliberg.network;

import java.io.Serializable;

import drfoliberg.task.Task;

public class Message implements Serializable {

	private static final long serialVersionUID = -483657531000641905L;

	private ClusterProtocol code;
	private Task task;

	public Message(ClusterProtocol code) {
		this.code = code;
	}

	public Message(Task t) {
		this.code = ClusterProtocol.TASK_REQUEST;
		this.task = t;
	}

	public ClusterProtocol getCode() {
		return code;
	}

	public Task getTask() {
		return task;
	}

}
