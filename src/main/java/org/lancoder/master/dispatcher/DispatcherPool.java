package org.lancoder.master.dispatcher;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;

public class DispatcherPool extends Pool<DispatchItem> implements DispatcherListener {

	private static final int MAX_DISPATCHERS = 5;

	private DispatcherListener listener;

	public DispatcherPool(DispatcherListener listener) {
		super(MAX_DISPATCHERS);
		this.listener = listener;
	}

	@Override
	protected Pooler<DispatchItem> getNewPooler() {
		return new Dispatcher(listener);
	}

	@Override
	public void taskAccepted(DispatchItem item) {
		this.listener.taskAccepted(item);
		refresh();
	}

	@Override
	public void taskRefused(DispatchItem item) {
		this.listener.taskRefused(item);
		refresh();
	}

}
