package org.lancoder.master.checker;

import org.lancoder.common.Node;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;

/**
 * Loose pool implementation for node checking with custom listener interface
 *
 */
public class NodeCheckerPool extends Pool<Node> {

	private static final int MAX_CHECKERS = 5;

	private NodeCheckerListener listener;

	public NodeCheckerPool(NodeCheckerListener listener) {
		super(MAX_CHECKERS);
		this.listener = listener;
	}

	@Override
	protected Pooler<Node> getPoolerInstance() {
		return new NodeChecker(listener);
	}

}
