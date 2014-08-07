package drfoliberg.master.checker;

import java.util.PriorityQueue;
import java.util.Queue;

import drfoliberg.common.Node;
import drfoliberg.common.Service;

public class NodeCheckerPool extends Service {

	private static final int MAX_CHECKERS = 5;

	final Queue<HttpChecker> checkers = new PriorityQueue<HttpChecker>(MAX_CHECKERS);
	ThreadGroup threads;
	private NodeCheckerListener listener;

	public NodeCheckerPool(NodeCheckerListener listener) {
		this.listener = listener;
		threads = new ThreadGroup("checkerThreads");
	}

	private boolean addNewChecker() {
		System.err.println("Creating new node checker thread");
		HttpChecker checker = new HttpChecker(listener);
		Thread t = new Thread(threads, checker);
		t.start();
		return this.checkers.offer(checker);
	}

	public synchronized void add(Node n) {
		boolean accepted = false;
		if (checkers.size() == 0) {
			addNewChecker();
		}
		while (!accepted) {
			HttpChecker checker = checkers.peek();
			if (checker.getQueueSize() > 1 && checkers.size() < MAX_CHECKERS) {
				addNewChecker();
			}
			checker = checkers.poll();
			accepted = checker.add(n);
			checkers.add(checker);
		}
	}

	@Override
	public void stop() {
		super.stop();
		while (checkers.peek() != null) {
			checkers.poll().stop();
		}
		threads.interrupt();
	}

}
