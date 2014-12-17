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

	private Object poolMonitor = new Object();

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

	private volatile int activeCount = 0;

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

	@Override
	public void run() {
		while (!close) {
			try {
				synchronized (poolMonitor) {
					poolMonitor.wait();
					if (!todo.isEmpty()) {
						T item = this.todo.poll();
						if (!dispatch(item)) {
							this.todo.addFirst(item);
						}
					}
					setActiveCount();

				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

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

	private synchronized final void setActiveCount() {
		int count = 0;
		for (Pooler<T> pooler : this.poolers) {
			if (pooler.isActive()) {
				count++;
			}
		}
		this.activeCount = count;
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
		return activeCount > 0;
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
			Thread thread = new Thread(threads, pooler);
			pooler.setThread(thread);
			thread.start();
			poolers.add(pooler);
			try {
				Thread.sleep(1); // wait for thread to start
			} catch (InterruptedException e) {
			}
		}
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
		return activeCount < threadLimit;
	}

	/**
	 * Try to add an item to the pool. If pool is not allowed to have a queue and all poolers are busy, return false.
	 * 
	 * @param element
	 *            The element to handle
	 * @return If element could be added to queue
	 */
	public synchronized boolean handle(T element) {
		boolean handled = false;
		if (!canQueue && todo.size() > 0) {
			System.err.printf("Warning pool %s seems to be overflowing. Current todo list has %s elements.", this
					.getClass().getSimpleName(), todo.size());
		}
		handled = dispatch(element);
		if (!handled) {
			handled = todo.add(element);
		}
		setActiveCount();
		return handled;
	}

	/**
	 * Sends task to a pooler or adds it back to the queue if no pooler can be used.
	 * 
	 * @param task
	 *            The work to dispatch
	 * 
	 * @return True if a pool worker accepted
	 */
	private synchronized boolean dispatch(T task) {
		boolean dispatched = true;
		Pooler<T> pooler = this.getAvailablePooler();
		if (pooler == null || !pooler.handle(task)) {
			dispatched = false;
		}
		return dispatched;
	}

	/**
	 * Called when a resource completed it's task and is now free. Notifies pool's thread to refresh it's state.
	 */
	public void completed() {
		synchronized (poolMonitor) {
			poolMonitor.notify();
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
