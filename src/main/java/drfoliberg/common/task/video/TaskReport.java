package main.java.drfoliberg.common.task.video;

import java.io.Serializable;

public class TaskReport implements Serializable {

	private static final long serialVersionUID = -8315284437551682238L;
	private VideoEncodingTask task;
	private String unid;

	public TaskReport(String unid, VideoEncodingTask task) {
		this.unid = unid;
		this.task = task;
	}

	public VideoEncodingTask getTask() {
		return task;
	}

	public String getUnid() {
		return unid;
	}
}
