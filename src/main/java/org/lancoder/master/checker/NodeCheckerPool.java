package org.lancoder.master.checker;

import java.util.ArrayList;

import org.lancoder.common.Node;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.TaskReport;

/**
 * Loose pool implementation for node checking with custom listener interface
 *
 */
public class NodeCheckerPool extends Pool<Node> implements NodeCheckerListener {

	private static final int MAX_CHECKERS = 5;

	private NodeCheckerListener listener;

	public NodeCheckerPool(NodeCheckerListener listener) {
		super(MAX_CHECKERS);
		this.listener = listener;
	}

	@Override
	protected Pooler<Node> getNewPooler() {
		return new NodeChecker(this);
	}

	@Override
	public ArrayList<Node> getNodes() {
		return this.listener.getNodes();
	}

	@Override
	public ArrayList<Node> getOnlineNodes() {
		return this.listener.getOnlineNodes();
	}

	@Override
	public void nodeDisconnected(Node n) {
		this.listener.nodeDisconnected(n);
	}

	@Override
	public void readTaskReports(ArrayList<TaskReport> taskReports) {
		this.listener.readTaskReports(taskReports);
	}

	@Override
	public void readStatusReport(StatusReport statusReport) {
		this.listener.readStatusReport(statusReport);
	}
}
