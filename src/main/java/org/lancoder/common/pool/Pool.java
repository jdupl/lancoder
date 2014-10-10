package org.lancoder.common.pool;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.lancoder.common.Service;

public abstract class Pool<T> extends Service {

	protected final HashMap<T, Pooler<T>> converters = new HashMap<>();
	protected final LinkedBlockingQueue<T> todo = new LinkedBlockingQueue<>();
	protected final ThreadGroup threads = new ThreadGroup("threads");
	protected final PoolListener<T> listener;
	private int threadLimit;

	public Pool(int threads, PoolListener<T> listener) {
		this.threadLimit = threads;
		this.listener = listener;
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
		for (Pooler<T> p : converters.values()) {
			if (!p.isActive()) {
				pooler = p;
			}
		}
		return pooler == null && hasFree() ? getNewPooler() : pooler;
	}

	protected boolean hasFree() {
		return converters.size() < threadLimit;
	}

	/**
	 * Add an item to the pool.
	 * 
	 * @param task
	 * @return
	 */
	public void handle(T task) {
		this.todo.add(task);
		refresh();
	}

	/**
	 * Try to give todo items to poolers.
	 */
	protected void refresh() {
		boolean caughtNull = false;
		while (todo.size() > 0 && hasFree() && !caughtNull) {
			Pooler<T> pooler = this.getFreePooler();
			if (pooler != null) {
				pooler.start(todo.poll());
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
		for (Pooler<T> converter : converters.values()) {
			converter.stop();
		}
		threads.interrupt();
	}

	// public void started(T e) {
	// listener.started(e);
	// }
	//
	// public void completed(T e) {
	// this.converters.remove(e);
	// listener.completed(e);
	// }
	//
	// public void failed(T e) {
	// this.converters.remove(e);
	// listener.completed(e);
	// }
	//
	// public void crash(Exception e) {
	// // TODO
	// e.printStackTrace();
	// }
}
