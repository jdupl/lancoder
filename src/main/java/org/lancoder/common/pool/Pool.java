package org.lancoder.common.pool;

import java.util.ArrayList;
import java.util.HashMap;

import org.lancoder.common.Service;

public abstract class Pool<T> extends Service implements PoolListener<T> {

	protected final HashMap<T, Pooler<T>> converters = new HashMap<>();
	protected final ArrayList<T> todo = new ArrayList<>();
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

	protected boolean hasFree() {
		return converters.size() < threadLimit;
	}

	public abstract boolean encode(T task);

	protected synchronized boolean spawn(Pooler<T> pooler) {
		if (!hasFree()) {
			return false;
		}
		converters.put(pooler.getPoolable(), pooler);
		new Thread(threads, pooler).start();
		return true;
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

	public void started(T e) {
		listener.started(e);
	}

	public void completed(T e) {
		this.converters.remove(e);
		listener.completed(e);
	}

	public void failed(T e) {
		this.converters.remove(e);
		listener.completed(e);
	}

	public void crash(Exception e) {
		// TODO
		e.printStackTrace();
	}
}
