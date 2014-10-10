package org.lancoder.common.pool;

import java.util.concurrent.LinkedBlockingDeque;

import org.lancoder.common.RunnableService;

public abstract class Pooler<T> extends RunnableService {

	private LinkedBlockingDeque<T> requests = new LinkedBlockingDeque<>();
	protected PoolListener<T> listener;
	protected T task;
	protected boolean active;

	public Pooler(PoolListener<T> listener) {
		this.listener = listener;
	}

	/**
	 * Get poolable element currently processed by pooler.
	 * 
	 * @return The element
	 */
	public T getPoolable() {
		return this.task;
	}

	public boolean isActive() {
		return this.active;
	}

	public void add(T request) {
		if (active) {
			throw new IllegalStateException("Pooler ressource is busy !");
		} else if (this.requests.size() > 0) {
			throw new IllegalStateException("Pooler queue is not empty !");
		} else {
			requests.add(request);
		}
	}

	/**
	 * Start the actual request. Assigns request to the pooler's current task. Calls custom start implementation of the
	 * pooler.
	 * 
	 * @param request
	 *            The request ready to start.
	 */
	private void handle(T request) {
		this.task = request;
		start();
	}

	protected abstract void start();

	/**
	 * While the pooler thread is running start tasks when requests gets elements.
	 */
	@Override
	public void run() {
		try {
			while (!close) {
				handle(requests.take());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
