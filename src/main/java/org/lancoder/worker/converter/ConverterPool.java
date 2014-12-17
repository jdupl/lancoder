package org.lancoder.worker.converter;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.PoolWorker;
import org.lancoder.common.task.ClientTask;

public abstract class ConverterPool<T extends ClientTask> extends Pool<T> {

	public ConverterPool(int threadLimit, boolean canQueue) {
		super(threadLimit, canQueue);
	}

	public synchronized void cancel(Object task) {
		for (PoolWorker<? extends ClientTask> poolWorker : this.workers) {
			if (poolWorker.isActive() && poolWorker.getPoolable().equals(task)) {
				poolWorker.cancelTask(task);
			}
		}
	}
}
