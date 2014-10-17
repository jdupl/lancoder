package org.lancoder.common.pool;

public interface PoolListener<T> {

	public void started(T e);

	public void completed(T e);

	public void failed(T e);

	public void crash(Exception e);

}
