package org.lancoder.worker;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.PoolWorker;
import org.lancoder.common.task.ClientTask;

public class TaskHandlerPool extends Pool<ClientTask> {

	private Worker worker;

	public TaskHandlerPool(Worker worker) {
		super(1, true);
		this.worker = worker;
	}

	@Override
	protected PoolWorker<ClientTask> getPoolWorkerInstance() {
		return new TaskHandler(worker);
	}

}

class TaskHandler extends PoolWorker<ClientTask> {

	private Worker worker;

	public TaskHandler(Worker worker) {
		this.worker = worker;
	}

	@Override
	protected void start() {
		worker.startWork(task);
	}
}
