package org.lancoder.master.checker;

import org.lancoder.common.Node;
import org.lancoder.common.Service;
import org.lancoder.common.events.EventListener;
import org.lancoder.common.scheduler.Schedulable;
import org.lancoder.master.NodeManager;

public class NodeCheckerService extends Schedulable implements Service {

	private final static int MS_DELAY_BETWEEN_CHECKS = 5000;
	private static final int MAX_CHECKERS = 5;

	private NodeCheckerPool pool;
	private Thread poolThread;
	private NodeManager nodeManager;

	public NodeCheckerService(EventListener listener, NodeManager nodeManager) {
		this.pool = new NodeCheckerPool(MAX_CHECKERS, listener);
		this.nodeManager = nodeManager;
	}

	private void checkNodes() {
		if (poolThread == null) {
			startPool();
		}

		if (!nodeManager.getNodes().isEmpty()) {
			for (Node n : nodeManager.getOnlineNodes()) {
				pool.add(n);
			}
		}
	}

	private void startPool() {
		poolThread = new Thread(pool, this.getClass().getSimpleName());
		poolThread.start();
	}

	@Override
	public void stop() {
		pool.stop();
		poolThread.interrupt();
	}

	@Override
	public long getMsRunDelay() {
		return MS_DELAY_BETWEEN_CHECKS;
	}

	@Override
	public void runTask() {
		this.checkNodes();
	}
}
