package org.lancoder.master.dispatcher;

import org.lancoder.common.Node;
import org.lancoder.common.task.Task;

public class DispatchItem {
	private Task task;

	private Node node;

	public DispatchItem(Task task, Node node) {
		this.task = task;
		this.node = node;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
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
