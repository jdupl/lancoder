package org.lancoder.common.pool;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.lancoder.common.RunnableService;

/**
 * Generic pool used to handle threaded tasks. Allows threads to be reused.
 * 
 * @author Justin Duplessis
 *
 * @param <T>
 *            The type of tasks to be handled by the pool
 */
public abstract class Pool<T> extends RunnableService implements Cleanable, PoolWorkerListener<T> {

	private Object refreshWaitLock = new Object();
	private Object poolRefresh = new Object();

	/**
	 * How many pool workers can be initialized in the pool
	 */
	private int threadLimit;
	/**
	 * The pool will accept tasks and send to a queue if no pool worker can be used. Otherwise,
	 */
	private boolean canQueue;
	/**
	 * List of the initialized pool workers in the pool
	 */
	protected final ArrayList<PoolWorker<T>> workers = new ArrayList<>();

	protected final ConcurrentLinkedDeque<PoolWorker<T>> freeWorkers = new ConcurrentLinkedDeque<>();

	/**
	 * Contains the tasks to send to pool workers
	 */
	protected final ConcurrentLinkedDeque<T> todo = new ConcurrentLinkedDeque<>();
	/**
	 * Thread group of the pool workers
	 */
	protected final ThreadGroup threads = new ThreadGroup("threads");

	/**
	 * Create a default pool with a defined thread limit. Pool will queue items without limitations.
	 * 
	 * @param threadLimit
	 *            The maximum number of pool workers to handle
	 */
	public Pool(int threadLimit) {
		this(threadLimit, true);
	}

	/**
	 * Create a pool with a defined thread limit.
	 * 
	 * @param threadLimit
	 *            The maximum number of pool workers to handle
	 * @param canQueue
	 *            False if pool should no pile up tasks
	 */
	public Pool(int threadLimit, boolean canQueue) {
		this.threadLimit = threadLimit;
		this.canQueue = canQueue;
	}

	/**
	 * Instantiate a pool worker without starting it.
	 * 
	 * @return The pool worker
	 */
	protected abstract PoolWorker<T> getPoolWorkerInstance();

	@Override
	public void run() {
		while (!close) {
			synchronized (poolRefresh) {
				try {
					poolRefresh.wait();
					refresh();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * Returns true if the pool should be cleaned.
	 */
	@Override
	public boolean shouldClean() {
		// As cleaning the pool involves logic from the pool workers, always assume we should clean the pool.
		return true;
	}

	/**
	 * Clean the resources of the pool. Allows the pool to shrink after higher load.
	 * 
	 * @return True if any resource was cleaned
	 */
	@Override
	public boolean clean() {
		ArrayList<PoolWorker<T>> toClean = new ArrayList<>();
		for (PoolWorker<T> poolWorker : workers) {
			if (poolWorker.shouldClean()) {
				toClean.add(poolWorker);
			}
		}
		for (PoolWorker<T> poolWorker : toClean) {
			poolWorker.clean();
			removeWorker(poolWorker);
		}
		return toClean.size() != 0;
	}

	private void removeWorker(PoolWorker<T> worker) {
		this.workers.remove(worker);
		this.freeWorkers.remove(worker);
	}

	public synchronized final int getActiveCount() {
		return workers.size() - freeWorkers.size();
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
	 * @return True if some pool workers are busy
	 */
	public boolean hasWorking() {
		return getActiveCount() > 0;
	}

	/**
	 * Try to spawn a new resource and run it in the main thread pool.
	 * 
	 * @return
	 */
	private PoolWorker<T> spawn() {
		PoolWorker<T> poolWorker = null;

		Object threadLock = new Object();

		if (canSpawn()) {
			poolWorker = getPoolWorkerInstance(this, threadLock);

			Thread thread = new Thread(threads, poolWorker);
			poolWorker.setThread(thread);

			thread.start();
			try {
				synchronized (threadLock) {
					threadLock.wait();
				}
			} catch (InterruptedException e) {
			}

			this.workers.add(poolWorker);
		}

		return poolWorker;
	}

	private final PoolWorker<T> getPoolWorkerInstance(PoolWorkerListener<T> workerListener, Object lock) {
		PoolWorker<T> ressource = getPoolWorkerInstance();
		ressource.setPoolWorkerListener(workerListener);
		ressource.setParentLock(lock);
		return ressource;
	}

	/**
	 * Checks if pool has free worker or worker slot
	 * 
	 * @return
	 */
	protected boolean hasFree() {
		return getActiveCount() < threadLimit;
	}

	/**
	 * Decides if pool has space to spawn a new pool worker.
	 * 
	 * @return True if pool can spawn a pool worker
	 */
	private boolean canSpawn() {
		return workers.size() < threadLimit;
	}

	/**
	 * Get a free pool worker resource or create a new one.
	 * 
	 * @return A free pool worker or null if no pool worker are available and pool is full
	 */
	private PoolWorker<T> getAvailableWorker() {
		PoolWorker<T> poolWorker = getFreeWorker();

		if (poolWorker == null) {
			poolWorker = spawn();
		}
		return poolWorker;
	}

	/**
	 * Get a currently free pool worker.
	 * 
	 * @return The free resource or null if none is available.
	 */
	private PoolWorker<T> getFreeWorker() {
		return freeWorkers.poll();
	}

	/**
	 * Try to add an item to the pool. If pool is not allowed to have a queue and all pool workers are busy, return
	 * false.
	 * 
	 * @param element
	 *            The element to handle
	 * @return If element could be added to queue
	 */
	public synchronized boolean add(T element) {
		// Ran from another thread
		boolean added = false;

		if (!canQueue && todo.size() > 0) {
			System.err.printf("Warning pool %s seems to be overflowing. Current todo list has %s elements.", this
					.getClass().getSimpleName(), todo.size());
		} else {
			added = this.todo.add(element);

			// Notify pool's thread to refresh
			synchronized (poolRefresh) {
				poolRefresh.notifyAll();
			}

			// Wait for pool refresh to complete as new resources may take time to load
			synchronized (refreshWaitLock) {
				try {
					refreshWaitLock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		return added;
	}

	/**
	 * Sends task to a pool worker or adds it back to the queue if no pool worker can be used.
	 * 
	 * @param task
	 *            The work to dispatch
	 * 
	 * @return True if a pool worker accepted
	 */
	private boolean dispatch(T task) {
		// Ran from Pool Thread
		PoolWorker<T> poolWorker = this.getAvailableWorker();

		if (poolWorker == null) {
			return false;
		}

		if (poolWorker.handle(task)) {
			return true;
		}

		freeWorkers.add(poolWorker);
		return false;
	}

	private void refresh() {
		// Ran from Pool Thread
		if (!todo.isEmpty()) {
			T item = this.todo.poll();

			if (!dispatch(item)) {
				this.todo.addFirst(item);
			}
		}
		// Notify threads waiting on the refresh monitor
		synchronized (refreshWaitLock) {
			refreshWaitLock.notifyAll();
		}
	}

	/**
	 * Called when a resource completed it's task and is now free. Notifies pool's thread to refresh it's state.
	 */
	public final void completed(PoolWorker<T> worker) {
		// Ran from PoolWorker thread
		freeWorkers.add((PoolWorker<T>) worker);
		synchronized (poolRefresh) {
			poolRefresh.notify();
		}
	}

	/**
	 * Gracefully close pool and it's resources by interrupting resources threads.
	 */
	@Override
	public void stop() {
		super.stop();
		for (PoolWorker<T> worker : workers) {
			worker.stop();
		}
		threads.interrupt();
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}

	public void setThreadLimit(int threadLimit) {
		this.threadLimit = threadLimit;
	}

}
