package org.lancoder.common.pool;

import java.util.concurrent.LinkedBlockingDeque;

import org.lancoder.common.RunnableService;

public abstract class Pooler<T> extends RunnableService {

	protected final static long CLEAN_DELAY_MSEC = 5 * 1000 * 60;

	/**
	 * List of elements to process
	 */
	private LinkedBlockingDeque<T> requests = new LinkedBlockingDeque<>();
	/**
	 * The listener to notify
	 */
	protected PoolListener<T> listener;
	/**
	 * Currently processed element
	 */
	protected T task;
	/**
	 * Is the ressource currently in use
	 */
	protected boolean active;
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

	public Pooler(PoolListener<T> listener) {
		this();
		this.listener = listener;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	/**
	 * Decide if the pooler should be closed. Super implementation provides a time based decision from last activity.
	 * 
	 * @return if current pooler should be closed
	 */
	private boolean expired() {
		return System.currentTimeMillis() - this.lastActivity > CLEAN_DELAY_MSEC;
	}

	/**
	 * Stop and clean ressource if necessary
	 */
	public final boolean clean() {
		boolean closed = false;
		if (!isActive() && expired()) {
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
		this.active = true;
		start(); // pooler thread is now busy and blocks here
		this.active = false;
		this.lastActivity = System.currentTimeMillis();
	}

	@Override
	public void stop() {
		super.stop();
		this.thread.interrupt();
	}

	protected abstract void start();

	/**
	 * While the pooler thread is running start tasks when requests gets elements.
	 */
	@Override
	public final void run() {
		try {
			while (!close) {
				handle(requests.take());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
