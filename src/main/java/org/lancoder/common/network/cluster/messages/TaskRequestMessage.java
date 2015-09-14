package org.lancoder.common.network.cluster.messages;

import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.task.ClientTask;

public class TaskRequestMessage extends Message {

	private static final long serialVersionUID = -994042578899999534L;

	public ClientTask task;

	/**
	 * Sent from master to workers
	 *
	 * @param task
	 *            The task to request to the worker
	 */
	public TaskRequestMessage(ClientTask task) {
		this(task, ClusterProtocol.TASK_REQUEST);
	}

	public TaskRequestMessage(ClientTask task, ClusterProtocol alt) {
		super(alt);
		this.task = task;
	}

	public ClientTask getTask() {
		return task;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TaskRequestMessage) {
			TaskRequestMessage other = (TaskRequestMessage) o;
			return this.task.equals(other.task);
		}
		return super.equals(o);
	}

	@Override
	public String toString() {
		return "TaskRequestMessage [task=" + task + "]";
	}

}
