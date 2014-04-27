package drfoliberg.common.network.messages;

import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.task.EncodingTask;

public class TaskRequestMessage extends Message {

	private static final long serialVersionUID = -994042578899999534L;

	public EncodingTask task;

	/**
	 * Sent from master to workers
	 * 
	 * @param task
	 *            The task to request to the worker
	 */
	public TaskRequestMessage(EncodingTask task) {
		super(ClusterProtocol.TASK_REQUEST);
		this.task = task;
	}
}
