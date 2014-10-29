package org.lancoder.master.checker;

import org.lancoder.common.Node;
import org.lancoder.common.RunnableService;
import org.lancoder.common.events.EventListener;
import org.lancoder.master.NodeManager;

public class NodeCheckerService extends RunnableService {

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
		if (nodeManager.getNodes().isEmpty()) {
			System.out.println("MASTER NODE CHECKER: no nodes to check!");
		} else {
			System.out.println("MASTER NODE CHECKER: checking if nodes are still alive");
			for (Node n : nodeManager.getOnlineNodes()) {
				pool.handle(n);
			}
		}
	}

	@Override
	public void stop() {
		super.stop();
		pool.stop();
		poolThread.interrupt();
	}

	@Override
	public void run() {
		System.out.println("Starting node checker service!");
		poolThread = new Thread(pool);
		poolThread.start();
		while (!close) {
			try {
				checkNodes();
				System.out.println("NODE CHECKER: checking back in 5 seconds");
				Thread.currentThread();
				Thread.sleep(MS_DELAY_BETWEEN_CHECKS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Closed node checker service !");
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}

}
