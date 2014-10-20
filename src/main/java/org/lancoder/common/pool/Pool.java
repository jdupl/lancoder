package org.lancoder.common.pool;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.lancoder.common.RunnableService;

public abstract class Pool<T> extends RunnableService implements PoolListener<T>, Cleanable {

	/**
	 * How many poolers can be initialized in the pool
	 */
	private int threadLimit;
	/**
	 * The pool will accept tasks and send to a queue if no pooler can be used. Otherwise,
	 */
	private boolean canQueue;
	/**
	 * List of the initialized poolers in the pool
	 */
	protected final ArrayList<Pooler<T>> poolers = new ArrayList<>();
	/**
	 * Contains the tasks to send to poolers
	 */
	protected final LinkedBlockingQueue<T> todo = new LinkedBlockingQueue<>();
	/**
	 * Thread group of the poolers
	 */
	protected final ThreadGroup threads = new ThreadGroup("threads");
	/**
	 * The listener of the pool
	 */
	protected PoolListener<T> listener;

	public Pool(int threadLimit, PoolListener<T> listener) {
		this(threadLimit, listener, true);
	}

	public Pool(int threadLimit, PoolListener<T> listener, boolean canQueue) {
		this.threadLimit = threadLimit;
		this.listener = listener;
		this.canQueue = canQueue;
	}

	public Pool(int threadLimit) {
		this.threadLimit = threadLimit;
	}

	@Override
	public boolean shouldClean() {
		return true;
	};

	/**
	 * Clean the resources of the pool. Allows the pool to shrink after high load.
	 * 
	 * @return if any resource was cleaned
	 */
	@Override
	public boolean clean() {
		ArrayList<Pooler<T>> toClean = new ArrayList<>();
		for (Pooler<T> pooler : poolers) {
			if (pooler.shouldClean()) {
				toClean.add(pooler);
			}
		}
		for (Pooler<T> pooler : toClean) {
			pooler.clean();
			this.poolers.remove(pooler);
		}
		return toClean.size() != 0;
	}

	/**
	 * Count the number of currently busy pooler resource
	 * 
	 * @return The busy pooler count
	 */
	public int getActiveCount() {
		int count = 0;
		for (Pooler<T> pooler : this.poolers) {
			if (pooler.isActive()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Public synchronized call to get hasFree
	 * 
	 * @return hasFree()
	 */
	public synchronized boolean hasFreeConverters() {
		return hasFree();
	}

	private Pooler<T> spawn() {
		if (!canSpawn()) {
			return null;
		}
		Pooler<T> pooler = getNewPooler();
		Thread thread = new Thread(threads, pooler);
		pooler.setThread(thread);
		thread.start();
		poolers.add(pooler);
		System.err.printf("%s spawned new pooler ressource. Now with %d poolers.%n", this.getClass().getSimpleName(),
				this.poolers.size());
		return pooler;
	}

	protected abstract Pooler<T> getNewPooler();

	private boolean canSpawn() {
		return poolers.size() < threadLimit;
	}

	/**
	 * Get a free pooler resource or create a new one.
	 * 
	 * @return A free pooler or null if no pooler are available.
	 */
	private Pooler<T> getFreePooler() {
		Pooler<T> pooler = null;
		for (Pooler<T> p : poolers) {
			if (!p.isActive()) {
				pooler = p;
			}
		}
		if (pooler == null) {
			pooler = spawn();
		}
		return pooler;
	}

	/**
	 * Get if any currently initialized pooler is free
	 * 
	 * @return
	 */
	protected boolean hasFree() {
		return getActiveCount() < threadLimit;
	}

	/**
	 * Try to add an item to the pool. If pool is not allowed to have a queue and all poolers are busy, return false.
	 * 
	 * @param element
	 *            The element to handle
	 * @return If element could be added to queue
	 */
	public boolean handle(T element) {
		return this.todo.add(element);
	}

	public String toString() {
		return String.format("%s has %d poolers and %d todos", this.getClass().getSimpleName(), this.poolers.size(),
				this.todo.size());
	}

	/**
	 * Sends task to a pooler or adds it back to the queue if no pooler can be used.
	 * 
	 * @param task
	 */
	private void dispatch(T task) {
		Pooler<T> pooler = this.getFreePooler();
		if (pooler != null) {
			pooler.add(task);
		} else {
			System.err.println("Warning: could not find free pooler ressource.");
		}
	}

	public void run() {
		while (!close) {
			try {
				dispatch(this.todo.take());
			} catch (InterruptedException e) {
				System.out.println("Pool interrupted");
			}
		}
	}

	public int getThreadLimit() {
		return threadLimit;
	}

	@Override
	public void stop() {
		super.stop();
		for (Pooler<T> converter : poolers) {
			converter.stop();
		}
		threads.interrupt();
	}

	public void started(T e) {
		listener.started(e);
	}

	public void completed(T e) {
		listener.completed(e);
	}

	public void failed(T e) {
		listener.failed(e);
	}

	public void crash(Exception e) {
		// TODO
		e.printStackTrace();
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}
}
