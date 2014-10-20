package org.lancoder.master.checker;

import org.lancoder.common.Node;
import org.lancoder.common.RunnableService;

public class NodeCheckerService extends RunnableService {

	private final static int MS_DELAY_BETWEEN_CHECKS = 5000;
	private NodeCheckerListener listener;
	private NodeCheckerPool pool;
	private Thread poolThread;

	public NodeCheckerService(NodeCheckerListener listener) {
		this.listener = listener;
		this.pool = new NodeCheckerPool(listener);
	}

	private void checkNodes() {
		if (listener.getNodes().isEmpty()) {
			System.out.println("MASTER NODE CHECKER: no nodes to check!");
		} else {
			System.out.println("MASTER NODE CHECKER: checking if nodes are still alive");
			for (Node n : listener.getOnlineNodes()) {
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
