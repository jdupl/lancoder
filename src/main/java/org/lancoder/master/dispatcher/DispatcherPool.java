package org.lancoder.master.dispatcher;

import java.util.ArrayList;

import org.eclipse.jetty.util.BlockingArrayQueue;
import org.lancoder.common.Service;
import org.lancoder.common.task.ClientTask;

public class DispatcherPool extends Service implements DispatcherListener {

	private static final int MAX_DISPATCHERS = 5;

	private BlockingArrayQueue<DispatchItem> toDispatch = new BlockingArrayQueue<>();
	private ArrayList<ObjectDispatcher> dispatchers = new ArrayList<>();
	private DispatcherListener listener;
	private ThreadGroup threads = new ThreadGroup("dispatcherThreads");

	public DispatcherPool(DispatcherListener listener) {
		this.listener = listener;
	}

	private ObjectDispatcher createNewDispatcher() {
		ObjectDispatcher dispatcher = new ObjectDispatcher(this);
		dispatchers.add(dispatcher);
		Thread t = new Thread(threads, dispatcher);
		t.start();
		return dispatcher;
	}

	private ObjectDispatcher getFreeDispatcher() {
		for (ObjectDispatcher dispatcher : dispatchers) {
			if (dispatcher.isFree()) {
				return dispatcher;
			}
		}
		if (dispatchers.size() < MAX_DISPATCHERS) {
			return createNewDispatcher();
		}
		return null;
	}

	private boolean taskInQueue(ClientTask task) {
		for (DispatchItem dispatchItem : toDispatch) {
			if (dispatchItem.getTask().equals(task)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Add an item to dispatch to queue and update list.
	 * 
	 * @param item
	 *            The item to dispatch
	 */
	public void dispatch(DispatchItem item) {
		if (taskInQueue(item.getTask())) {
			System.err.println("Concurrency error avoided !");
		} else {
			toDispatch.add(item);
		}
		update();
	}

	/**
	 * Check if queue has anything and if dispatchers are free and dispatcher first item in queue. Must be called every
	 * time that the item list is updated and node respond.
	 */
	private synchronized void update() {
		DispatchItem item = null;
		ObjectDispatcher dispatcher = null;
		if ((item = toDispatch.peek()) != null && (dispatcher = getFreeDispatcher()) != null) {
			if (dispatcher.queue(item)) {
				toDispatch.poll();
			}
		}
	}

	@Override
	public void taskRefused(DispatchItem item) {
		this.listener.taskRefused(item);
		update();
	}

	@Override
	public void taskAccepted(DispatchItem item) {
		this.listener.taskAccepted(item);
		update();
	}

	@Override
	public void stop() {
		super.stop();
		this.threads.interrupt();
	}

}
