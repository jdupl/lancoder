package org.lancoder.common.task.video;

import java.io.Serializable;

import org.lancoder.common.task.Task;

public class TaskReport implements Serializable {

	private static final long serialVersionUID = -8315284437551682238L;
	private Task task;
	private String unid;

	public TaskReport(String unid, Task task) {
		this.unid = unid;
		this.task = task;
	}

	public Task getTask() {
		return task;
	}

	public String getUnid() {
		return unid;
	}
}
