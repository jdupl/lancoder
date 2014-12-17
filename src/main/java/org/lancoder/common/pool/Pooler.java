package org.lancoder.common.pool;

import java.util.concurrent.LinkedBlockingDeque;

import org.lancoder.common.RunnableService;

public abstract class Pooler<T> extends RunnableService implements Cleanable {

	/**
	 * List of elements to process
	 */
	private volatile LinkedBlockingDeque<T> requests = new LinkedBlockingDeque<>();
	/**
	 * Currently processed element
	 */
	protected T task;
	/**
	 * Is the ressource currently in use
	 */
	protected volatile boolean active;
	/**
	 * Timestamp in unix msec of last activity
	 */
	private long lastActivity;

	/**
	 * The thread used by the pooler ressource
	 */
	private Thread thread;

	public Pooler() {
		this.lastActivity = System.currentTimeMillis();
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	/**
	 * Decide if the pooler should be closed. Super implementation provides a time based decision from last activity.
	 * 
	 * @return True if current pooler should be closed
	 */
	private boolean expired() {
		return System.currentTimeMillis() - this.lastActivity > CLEAN_DELAY_MSEC;
	}

	@Override
	public boolean shouldClean() {
		return !isActive() && expired();
	}

	/**
	 * Stop and clean ressource if necessary
	 */
	public final boolean clean() {
		boolean closed = false;
		if (shouldClean()) {
			this.stop();
			closed = true;
		}
		return closed;
	}

	/**
	 * Get poolable element currently processed by pooler.
	 * 
	 * @return The element
	 */
	public synchronized T getPoolable() {
		return this.task;
	}

	/**
	 * Returns the state of the worker.
	 * 
	 * @return True is pooler is busy.
	 */
	public synchronized boolean isActive() {
		return this.active;
	}

	/**
	 * Check if requests queue is empty. Pooler should not pile up items.
	 * 
	 * @return True if empty.
	 */
	public synchronized boolean isFree() {
		return this.requests.isEmpty() && !isActive();
	}

	/**
	 * Add a task to the pooler.
	 * 
	 * @param request
	 *            The task to complete
	 */
	public synchronized boolean add(T request) {
		boolean handled = false;
		if (!isFree()) {
			System.err.printf("Warning: pooler ressource %s now has %d tasks in backlog.%n", this.getClass()
					.getSimpleName(), this.requests.size());
		} else if (isActive()) {
			System.err.printf("Pooler ressource is busy !");
		} else {
			handled = requests.add(request);
		}
		return handled;
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
		start(); // pooler thread is now busy and blocks here
		this.lastActivity = System.currentTimeMillis();
		this.task = null;
	}

	@Override
	public void stop() {
		super.stop();
		this.thread.interrupt();
	}

	public void cancelTask(Object task) {
		if (this.task != null && this.task.equals(task)) {
			throw new UnsupportedOperationException("Task cancellation is not supported for "
					+ this.getClass().getSimpleName() + " !");
		}
	}

	protected abstract void start();

	/**
	 * While the pooler thread is running start tasks when requests gets elements.
	 */
	@Override
	public final void run() {
		try {
			while (!close) {
				T task = requests.take();
				active = true;
				handle(task);
				active = false;
			}
		} catch (InterruptedException e) {
		}
	}
}
