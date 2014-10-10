package org.lancoder.common.pool;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.lancoder.common.Service;

public abstract class Pool<T> extends Service implements PoolListener<T> {

	protected final HashMap<T, Pooler<T>> poolers = new HashMap<>();
	protected final LinkedBlockingQueue<T> todo = new LinkedBlockingQueue<>();
	protected final ThreadGroup threads = new ThreadGroup("threads");
	protected final PoolListener<T> listener;
	private int threadLimit;
	private boolean canQueue;

	public Pool(int threadLimit, PoolListener<T> listener) {
		this(threadLimit, listener, true);
	}

	public Pool(int threadLimit, PoolListener<T> listener, boolean canQueue) {
		this.threadLimit = threadLimit;
		this.listener = listener;
		this.canQueue = canQueue;
	}

	/**
	 * Public synchronized call to get hasFree
	 * 
	 * @return hasFree()
	 */
	public synchronized boolean hasFreeConverters() {
		return hasFree();
	}

	protected abstract Pooler<T> getNewPooler();

	/**
	 * Get a free pooler resource or create a new one.
	 * 
	 * @return A free pooler or null if no pooler are available.
	 */
	private Pooler<T> getFreePooler() {
		Pooler<T> pooler = null;
		for (Pooler<T> p : poolers.values()) {
			if (!p.isActive()) {
				pooler = p;
			}
		}
		return pooler == null && hasFree() ? getNewPooler() : pooler;
	}

	protected boolean hasFree() {
		return poolers.size() < threadLimit;
	}

	/**
	 * Try to add an item to the pool. If pool is not allowed to have a queue and all poolers are busy, return false.
	 * 
	 * @param element
	 *            The element to handle
	 * @return If element could be added to queue
	 */
	public boolean handle(T element) {
		boolean handled = false;
		if (canQueue || (todo.size() == 0 && hasFree())) {
			this.todo.add(element);
			refresh();
			handled = true;
		}
		return handled;
	}

	/**
	 * Try to give todo items to poolers.
	 */
	protected void refresh() {
		boolean caughtNull = false;
		while (todo.size() > 0 && hasFree() && !caughtNull) {
			Pooler<T> pooler = this.getFreePooler();
			if (pooler != null) {
				pooler.add(todo.poll());
			} else {
				caughtNull = true;
			}
		}
	}

	public synchronized int getThreadLimit() {
		return threadLimit;
	}

	@Override
	public void stop() {
		super.stop();
		for (Pooler<T> converter : poolers.values()) {
			converter.stop();
		}
		threads.interrupt();
	}

	public void started(T e) {
		listener.started(e);
	}

	public void completed(T e) {
		listener.completed(e);
		refresh();
	}

	public void failed(T e) {
		listener.failed(e);
		refresh();
	}

	public void crash(Exception e) {
		// TODO
		e.printStackTrace();
	}
}
