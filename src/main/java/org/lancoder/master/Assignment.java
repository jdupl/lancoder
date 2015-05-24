package org.lancoder.master;

import org.lancoder.common.Node;
import org.lancoder.common.task.ClientTask;

public class Assignment {

	private ClientTask task;
	private long time;
	private Node assignee;

	public Assignment(ClientTask task, Node assignee) {
		this.task = task;
		this.time = System.currentTimeMillis();
		this.assignee = assignee;
	}

	public ClientTask getTask() {
		return task;
	}

	public long getTime() {
		return time;
	}

	public Node getAssignee() {
		return assignee;
	}

}
