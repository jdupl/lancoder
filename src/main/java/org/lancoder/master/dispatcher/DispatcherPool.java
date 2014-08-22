package org.lancoder.master.dispatcher;

import java.util.ArrayList;

import org.eclipse.jetty.util.BlockingArrayQueue;
import org.lancoder.common.Service;

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
		System.err.println("Creating new dispatcher");
		ObjectDispatcher dispatcher = new ObjectDispatcher(listener);
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
		System.err.println("Maximum dispatcher reached.");
		return null;
	}

	public synchronized void dispatch(DispatchItem item) {
		ObjectDispatcher dispatcher = getFreeDispatcher();
		if (dispatcher != null) {
			dispatcher.queue(item);
		} else {
			toDispatch.add(item);
		}
	}

	private synchronized void update() {
		DispatchItem item = null;
		ObjectDispatcher dispatcher = null;
		if ((item = toDispatch.poll()) != null && (dispatcher = getFreeDispatcher()) != null) {
			dispatcher.queue(item);
		}
	}

	@Override
	public void taskRefused(DispatchItem item) {
		this.toDispatch.remove(item);
		this.listener.taskRefused(item);
		update();
	}

	@Override
	public void taskAccepted(DispatchItem item) {
		this.toDispatch.remove(item);
		this.listener.taskAccepted(item);
		update();
	}

	@Override
	public void stop() {
		super.stop();
		this.threads.interrupt();
	}

}
