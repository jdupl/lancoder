package org.lancoder.master.dispatcher;

import org.lancoder.common.Node;
import org.lancoder.common.task.ClientTask;

public class DispatchItem {

	private ClientTask task;
	private Node node;

	public DispatchItem(ClientTask task, Node node) {
		this.task = task;
		this.node = node;
	}

	public ClientTask getTask() {
		return task;
	}

	public void setTask(ClientTask task) {
		this.task = task;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof DispatchItem) {
			DispatchItem other = (DispatchItem) obj;
			return other.getNode().equals(this.getNode()) && other.getTask().equals(this.getTask());
		}
		return super.equals(obj);
	}

}
