package org.lancoder.master.dispatcher;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;

public class DispatcherPool extends Pool<DispatchItem> {

	private static final int MAX_DISPATCHERS = 5;

	private DispatcherListener listener;

	public DispatcherPool(DispatcherListener listener) {
		super(MAX_DISPATCHERS);
		this.listener = listener;
	}

	@Override
	protected Pooler<DispatchItem> getPoolerInstance() {
		return new Dispatcher(listener);
	}

}
