package org.lancoder.common.pool;

import org.lancoder.common.RunnableService;

public abstract class Pooler<T> extends RunnableService {

	protected boolean active;
	protected PoolListener<T> listener;

	public Pooler(PoolListener<T> listener) {
		this.listener = listener;
	}

	public abstract T getPoolable();

	public abstract boolean isActive();
}
