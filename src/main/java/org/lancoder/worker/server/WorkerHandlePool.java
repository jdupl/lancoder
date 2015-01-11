package org.lancoder.worker.server;

import java.net.Socket;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.PoolWorker;

public class WorkerHandlePool extends Pool<Socket> {

	private WorkerServerListener listener;

	public WorkerHandlePool(int threadLimit, WorkerServerListener listener) {
		super(threadLimit);
		this.listener = listener;
	}

	@Override
	protected PoolWorker<Socket> getPoolWorkerInstance() {
		return new WorkerHandler(listener);
	}
}
