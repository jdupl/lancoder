package org.lancoder.master.api.node;

import java.net.Socket;

import org.lancoder.common.events.EventListener;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.PoolWorker;
import org.lancoder.master.NodeManager;

public class MasterHandlePool extends Pool<Socket> {

	private NodeManager nodeManager;
	private EventListener listener;

	public MasterHandlePool(int limit, NodeManager nodeManager, EventListener listener) {
		super(limit);
		this.nodeManager = nodeManager;
		this.listener = listener;
	}

	@Override
	protected PoolWorker<Socket> getPoolWorkerInstance() {
		return new MasterHandler(listener, nodeManager);
	}

}
