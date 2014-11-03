package org.lancoder.master.api.node;

import java.net.Socket;

import org.lancoder.common.events.EventListener;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;
import org.lancoder.master.NodeManager;

public class MasterHandlePool extends Pool<Socket> {

	private final static int MAX_HANDLERS = 100;

	private NodeManager nodeManager;
	private EventListener listener;

	public MasterHandlePool(NodeManager nodeManager, EventListener listener) {
		super(MAX_HANDLERS);
		this.nodeManager = nodeManager;
		this.listener = listener;
	}

	@Override
	protected Pooler<Socket> getPoolerInstance() {
		return new MasterHandler(listener, nodeManager);
	}

}
