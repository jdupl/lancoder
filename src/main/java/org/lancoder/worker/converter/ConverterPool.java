package org.lancoder.worker.converter;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.PoolListener;
import org.lancoder.common.pool.Pooler;

public abstract class ConverterPool<T> extends Pool<T> implements PoolListener<T> {

	public ConverterPool(int threads, PoolListener<T> listener) {
		super(threads, listener);
		// TODO
	}

	@Override
	public void started(T e) {
		listener.started(e);
	}

	@Override
	public void completed(T e) {
		this.converters.remove(e);
		listener.completed(e);
	}

	@Override
	public void failed(T e) {
		this.converters.remove(e);
		listener.completed(e);
	}

	@Override
	public void crash(Exception e) {
		// TODO
	}

}
