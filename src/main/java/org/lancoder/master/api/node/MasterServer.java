package org.lancoder.master.api.node;

import org.lancoder.common.events.EventListener;
import org.lancoder.common.network.cluster.Server;
import org.lancoder.master.NodeManager;

public class MasterServer extends Server {

	private EventListener listener;
	private NodeManager nodeManager;

	public MasterServer(int port, EventListener listener, NodeManager nodeManager) {
		super(port);
		this.listener = listener;
		this.nodeManager = nodeManager;
	}

	@Override
	protected void instanciatePool() {
		this.pool = new MasterHandlePool(MAX_HANDLERS, nodeManager, listener);
	}

}
