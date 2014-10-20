package org.lancoder.worker.server;

import java.net.Socket;

import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.ClientTask;

public class HandlerPool extends Pool<Socket> implements WorkerServerListener {

	private WorkerServerListener listener;

	public HandlerPool(int threadLimit, WorkerServerListener listener) {
		super(threadLimit);
		this.listener = listener;
	}

	@Override
	protected Pooler<Socket> getNewPooler() {
		return new WorkerHandler(this);
	}

	@Override
	public boolean taskRequest(ClientTask tqm) {
		return this.listener.taskRequest(tqm);
	}

	@Override
	public boolean deleteTask(ClientTask tqm) {
		return this.listener.deleteTask(tqm);
	}

	@Override
	public StatusReport statusRequest() {
		return this.listener.statusRequest();
	}

	@Override
	public void shutdownWorker() {
		this.listener.shutdownWorker();
	}

}
