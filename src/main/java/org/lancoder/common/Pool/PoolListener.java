package org.lancoder.common.Pool;

public interface PoolListener<T> {

	public void started(T e);

	public void completed(T e);

	public void failed(T e);
}
