package org.lancoder.common.pool;

public interface PoolWorkerListener<T> {
	
	public void completed(PoolWorker<T> worker); 
}
