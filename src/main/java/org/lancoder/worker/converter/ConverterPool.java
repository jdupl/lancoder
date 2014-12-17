package org.lancoder.worker.converter;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.ClientTask;

public abstract class ConverterPool<T extends ClientTask> extends Pool<T> {

	public ConverterPool(int threadLimit, boolean canQueue) {
		super(threadLimit, canQueue);
	}

	public synchronized void cancel(Object task) {
		for (Pooler<? extends ClientTask> pooler : this.poolers) {
			if (pooler.isActive() && pooler.getPoolable().equals(task)) {
				pooler.cancelTask(task);
			}
		}
	}
}
