package org.lancoder.master.checker;

import java.util.PriorityQueue;
import java.util.Queue;

import org.lancoder.common.Node;
import org.lancoder.common.Service;

@Deprecated
public class NodeCheckerPool extends Service {

	private static final int MAX_CHECKERS = 5;

	// TODO
	private final Queue<ObjectChecker> checkers = new PriorityQueue<ObjectChecker>(MAX_CHECKERS);
	private ThreadGroup threads = new ThreadGroup("checkerThreads");
	private NodeCheckerListener listener;

	public NodeCheckerPool(NodeCheckerListener listener) {
		this.listener = listener;
	}

	private boolean addNewChecker() {
		System.err.println("Creating new node checker thread");
		ObjectChecker checker = new ObjectChecker(listener);
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
			ObjectChecker checker = checkers.peek();
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
