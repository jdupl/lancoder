package org.lancoder.worker.server;

import org.lancoder.common.network.cluster.Server;

public class WorkerServer extends Server {

	WorkerServerListener listener;

	public WorkerServer(WorkerServerListener listener, int port) {
		super(port);
		this.listener = listener;
	}

	@Override
	protected void instanciatePool() {
		this.pool = new WorkerHandlePool(MAX_HANDLERS, listener);
	}

}
