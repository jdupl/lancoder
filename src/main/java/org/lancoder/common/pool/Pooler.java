package org.lancoder.common.pool;

import org.lancoder.common.RunnableService;

public abstract class Pooler<T> extends RunnableService {

	protected boolean active;
	protected PoolListener<T> listener;
	protected T task;

	public Pooler(T task, PoolListener<T> listener) {
		this.task = task;
		this.listener = listener;
	}

	public T getPoolable() {
		return this.task;
	}

	public boolean isActive() {
		return this.active;
	}
}
