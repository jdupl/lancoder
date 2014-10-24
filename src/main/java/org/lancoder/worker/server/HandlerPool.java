package org.lancoder.worker.server;

import java.net.Socket;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;

public class HandlerPool extends Pool<Socket> {

	private WorkerServerListener listener;

	public HandlerPool(int threadLimit, WorkerServerListener listener) {
		super(threadLimit);
		this.listener = listener;
	}

	@Override
	protected Pooler<Socket> getPoolerInstance() {
		return new WorkerHandler(listener);
	}
}
