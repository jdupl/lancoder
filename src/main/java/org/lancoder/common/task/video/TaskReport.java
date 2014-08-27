package org.lancoder.common.task.video;

import java.io.Serializable;

import org.lancoder.common.task.ClientTask;

public class TaskReport implements Serializable {

	private static final long serialVersionUID = -8315284437551682238L;
	private ClientTask task;
	private String unid;

	public TaskReport(String unid, ClientTask task) {
		this.unid = unid;
		this.task = task;
	}

	public ClientTask getTask() {
		return task;
	}

	public String getUnid() {
		return unid;
	}
}
