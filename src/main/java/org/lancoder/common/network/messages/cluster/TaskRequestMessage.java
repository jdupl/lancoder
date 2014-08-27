package org.lancoder.common.network.messages.cluster;

import org.lancoder.common.network.messages.ClusterProtocol;
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
		super(ClusterProtocol.TASK_REQUEST);
		this.task = task;
	}

	public ClientTask getTask() {
		return task;
	}
}
