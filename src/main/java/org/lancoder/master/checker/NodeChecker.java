package org.lancoder.master.checker;

import org.lancoder.common.Node;
import org.lancoder.common.RunnableService;

public class NodeChecker extends RunnableService {

	private final static int MS_DELAY_BETWEEN_CHECKS = 5000;
	private NodeCheckerListener listener;
	private NodeCheckerPool pool;

	public NodeChecker(NodeCheckerListener listener) {
		this.listener = listener;
		this.pool = new NodeCheckerPool(listener);
	}

	private boolean checkNodes() {
		if (listener.getNodes().size() == 0) {
			System.out.println("MASTER NODE CHECKER: no nodes to check!");
			return false;
		}
		System.out.println("MASTER NODE CHECKER: checking if nodes are still alive");
		for (Node n : listener.getOnlineNodes()) {
			pool.handle(n);
		}
		return false;
	}

	@Override
	public void run() {
		System.out.println("Starting node checker service!");
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
