package org.lancoder.common.pool;

import java.util.concurrent.atomic.AtomicBoolean;

import org.lancoder.common.RunnableService;

public abstract class PoolWorker<T> extends RunnableService implements Cleanable {

	private Object monitor = new Object();

	private Object parentLock;

	/**
	 * Current state of the worker
	 */
	private AtomicBoolean active = new AtomicBoolean(false);

	/**
	 * Currently processed element
	 */
	protected T task;
	/**
	 * Timestamp in unix msec of last activity
	 */
	private long lastActivity;

	/**
	 * The thread used by the worker resource
	 */
	private Thread thread;

	/**
	 * The pool to notify on task completion
	 */
	private PoolWorkerListener<T> pool;

	/**
	 * Constructor of a base worker resource. Sets last activity time to current time.
	 * 
	 */
	public PoolWorker() {
		this.lastActivity = System.currentTimeMillis();
	}

	/**
	 * Set the parent worker listening to this ressource.
	 * 
	 * @param pool
	 *            The parent pool waiting for this resource to perform an action
	 */
	public void setPoolWorkerListener(PoolWorkerListener<T> pool) {
		this.pool = pool;
	}

	/**
	 * Allow worker to know it's execution thread.
	 * 
	 * @param thread
	 *            The execution thread of the resource
	 */
	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public Thread getThread() {
		return thread;
	}

	/**
	 * Decide if the worker should be closed. Super implementation provides a time based decision from last activity.
	 * 
	 * @return True if current worker should be closed
	 */
	private boolean expired() {
		return System.currentTimeMillis() - this.lastActivity > CLEAN_DELAY_MSEC;
	}

	@Override
	public boolean shouldClean() {
		return !isActive() && expired();
	}

	/**
	 * Stop and clean resource if necessary.
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
	 * Get the task currently processed by the worker.
	 * 
	 * @return The task
	 */
	public synchronized T getPoolable() {
		return this.task;
	}

	/**
	 * Returns the state of the worker.
	 * 
	 * @return True is worker is busy
	 */
	public boolean isActive() {
		return active.get();
	}

	/**
	 * Check if worker is empty
	 * 
	 * @return True if empty
	 */
	public boolean isFree() {
		return !isActive();
	}

	/**
	 * Add a task to the worker.
	 * 
	 * @param request
	 *            The task to complete
	 */
	public boolean handle(T request) {
		boolean handled = false;
		if (!active.get()) {
			this.task = request;
			synchronized (monitor) {
				monitor.notify();
			}
			handled = true;
		}
		return handled;
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
	 * While the worker thread is running start tasks when requests gets elements.
	 */
	@Override
	public final void run() {
		try {
			while (!close) {
				synchronized (monitor) {
					unlockParent(); // put this before the while and it creates concurrency errors
					monitor.wait();
					active.set(true);
					start(); // Pool worker thread is now busy and blocks here

					lastActivity = System.currentTimeMillis();
					active.set(false);
					task = null;
					pool.completed(this);
				}
			}
		} catch (InterruptedException e) {
		}
	}

	private void unlockParent() {
		if (parentLock != null) {
			synchronized (parentLock) {
				parentLock.notifyAll();
			}
			parentLock = null;
		}
	}

	public void setParentLock(Object lock) {
		this.parentLock = lock;
	}
}
