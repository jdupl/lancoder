package org.lancoder.common.pool;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

import org.lancoder.common.RunnableService;

/**
 * Generic pool used to handle threaded tasks. Allows threads to be reused.
 * 
 * @author Justin Duplessis
 *
 * @param <T>
 *            The type of tasks to be handled by the pool
 */
public abstract class Pool<T> extends RunnableService implements Cleanable, PoolerListener {

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
	protected final LinkedBlockingDeque<T> todo = new LinkedBlockingDeque<>();
	/**
	 * Thread group of the poolers
	 */
	protected final ThreadGroup threads = new ThreadGroup("threads");

	/**
	 * Create a default pool with a defined thread limit. Pool will queue items without limitations.
	 * 
	 * @param threadLimit
	 *            The maximum number of poolers to handle
	 */
	public Pool(int threadLimit) {
		this(threadLimit, true);
	}

	/**
	 * Create a pool with a defined thread limit.
	 * 
	 * @param threadLimit
	 *            The maximum number of poolers to handle
	 * @param canQueue
	 *            False if pool should no pile up tasks
	 */
	public Pool(int threadLimit, boolean canQueue) {
		this.threadLimit = threadLimit;
		this.canQueue = canQueue;
	}

	public void completed() {
		// TODO
	}

	/**
	 * Returns true if the pool should be cleaned.
	 */
	@Override
	public boolean shouldClean() {
		// As cleaning the pool involves logic from the poolers, always assume we should clean the pool.
		return true;
	}

	/**
	 * Clean the resources of the pool. Allows the pool to shrink after higher load.
	 * 
	 * @return True if any resource was cleaned
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
	public synchronized int getActiveCount() {
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

	/**
	 * Public synchronized call to known if pool is working.
	 * 
	 * @return True if some poolers are busy
	 */
	public synchronized boolean hasWorking() {
		return getActiveCount() > 0;
	}

	/**
	 * Try to spawn a new resource and run it in the main thread pool.
	 * 
	 * @return
	 */
	private Pooler<T> spawn() {
		Pooler<T> pooler = null;
		if (canSpawn()) {
			pooler = getPoolerInstance(this);
		} else {
			System.err.printf("A maximum of %d element(s) has been reached in pool %s ! Cannot create new instance.",
					threadLimit, this.getClass().getSimpleName());
		}
		Thread thread = new Thread(threads, pooler);
		pooler.setThread(thread);
		thread.start();
		poolers.add(pooler);
		return pooler;
	}

	/**
	 * Instanciate a pooler ressource without starting it.
	 * 
	 * @return The pooler ressource
	 */
	protected abstract Pooler<T> getPoolerInstance();

	private final Pooler<T> getPoolerInstance(PoolerListener poolerListener) {
		Pooler<T> ressource = getPoolerInstance();
		ressource.setPoolerListener(poolerListener);
		return ressource;
	}

	/**
	 * Decides if pool has space to spawn a new ressource.
	 * 
	 * @return True if pool can spawn a ressource
	 */
	private boolean canSpawn() {
		return poolers.size() < threadLimit;
	}

	/**
	 * Get a free pooler resource or create a new one.
	 * 
	 * @return A free pooler or null if no pooler are available and pool is full
	 */
	private Pooler<T> getAvailablePooler() {
		Pooler<T> pooler = getFreePooler();
		if (pooler == null) {
			pooler = spawn();
		}
		return pooler;
	}

	/**
	 * Get a currently free pooler.
	 * 
	 * @return The free ressource or null if none is avaiable.
	 */
	private synchronized Pooler<T> getFreePooler() {
		Pooler<T> pooler = null;
		for (Pooler<T> p : poolers) {
			if (p.isFree()) {
				pooler = p;
			}
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
	public synchronized boolean handle(T element) {
		if (!canQueue && todo.size() > 0) {
			System.err.printf("Warning pool %s seems to be overflowing. Current todo list has %s elements.", this
					.getClass().getSimpleName(), todo.size());
		}
		return todo.add(element);
	}

	/**
	 * Sends task to a pooler or adds it back to the queue if no pooler can be used.
	 * 
	 * @param task
	 */
	private synchronized void dispatch(T task) {
		Pooler<T> pooler = this.getAvailablePooler();
		if (pooler == null || !pooler.add(task)) {
			System.err.println("Warning: could not find free pooler ressource.");
			try {
				Thread.sleep(500);
				todo.addFirst(task);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Actually start the pool and start accepting tasks.
	 */
	public void run() {
		while (!close) {
			try {
				dispatch(this.todo.take());
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Gracefully close pool and it's resources by interrupting resources threads.
	 */
	@Override
	public void stop() {
		super.stop();
		for (Pooler<T> ressource : poolers) {
			ressource.stop();
		}
		threads.interrupt();
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}
}
