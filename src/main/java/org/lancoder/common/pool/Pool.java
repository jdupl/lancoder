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
public abstract class Pool<T> extends RunnableService implements Cleanable, PoolWorkerListener {

	private Object poolMonitor = new Object();

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
	/**
	 * Contains the tasks to send to pool workers
	 */
	protected final LinkedBlockingDeque<T> todo = new LinkedBlockingDeque<>();
	/**
	 * Thread group of the pool workers
	 */
	protected final ThreadGroup threads = new ThreadGroup("threads");

	private volatile int activeCount = 0;

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
			this.workers.remove(poolWorker);
		}
		return toClean.size() != 0;
	}

	private synchronized final void setActiveCount() {
		int count = 0;
		for (PoolWorker<T> poolWorker : this.workers) {
			if (poolWorker.isActive()) {
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
	 * @return True if some pool workers are busy
	 */
	public synchronized boolean hasWorking() {
		return activeCount > 0;
	}

	/**
	 * Try to spawn a new resource and run it in the main thread pool.
	 * 
	 * @return
	 */
	private PoolWorker<T> spawn() {
		PoolWorker<T> poolWorker = null;
		if (canSpawn()) {
			poolWorker = getPoolWorkerInstance(this);
			Thread thread = new Thread(threads, poolWorker);
			poolWorker.setThread(thread);
			thread.start();
			workers.add(poolWorker);
			try {
				Thread.sleep(1); // wait for thread to start
			} catch (InterruptedException e) {
			}
		}
		return poolWorker;
	}

	/**
	 * Instanciate a pool worker ressource without starting it.
	 * 
	 * @return The pool worker
	 */
	protected abstract PoolWorker<T> getPoolWorkerInstance();

	private final PoolWorker<T> getPoolWorkerInstance(PoolWorkerListener workerListener) {
		PoolWorker<T> ressource = getPoolWorkerInstance();
		ressource.setPoolWorkerListener(workerListener);
		return ressource;
	}

	/**
	 * Decides if pool has space to spawn a new ressource.
	 * 
	 * @return True if pool can spawn a ressource
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
	private synchronized PoolWorker<T> getFreeWorker() {
		PoolWorker<T> poolWorker = null;
		for (PoolWorker<T> p : workers) {
			if (p.isFree()) {
				poolWorker = p;
			}
		}
		return poolWorker;
	}

	/**
	 * Get if any currently initialized pool worker is free
	 * 
	 * @return
	 */
	protected boolean hasFree() {
		return activeCount < threadLimit;
	}

	/**
	 * Try to add an item to the pool. If pool is not allowed to have a queue and all pool workers are busy, return
	 * false.
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
	 * Sends task to a pool worker or adds it back to the queue if no pool worker can be used.
	 * 
	 * @param task
	 *            The work to dispatch
	 * 
	 * @return True if a pool worker accepted
	 */
	private synchronized boolean dispatch(T task) {
		boolean dispatched = true;
		PoolWorker<T> poolWorker = this.getAvailableWorker();
		if (poolWorker == null || !poolWorker.handle(task)) {
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
		for (PoolWorker<T> ressource : workers) {
			ressource.stop();
		}
		threads.interrupt();
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}

}
