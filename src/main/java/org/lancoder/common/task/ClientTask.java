package org.lancoder.common.task;

import java.io.Serializable;

import org.lancoder.common.progress.TaskProgress;
import org.lancoder.common.progress.Unit;

public class ClientTask implements Serializable {

	private static final long serialVersionUID = 7072947025021592662L;
	protected PrototypeTask task;
	protected StreamConfig streamConfig;

	public ClientTask(PrototypeTask task, StreamConfig streamConfig) {
		this.task = task;
		this.streamConfig = streamConfig;
	}

	public PrototypeTask getTask() {
		return task;
	}

	public StreamConfig getStreamConfig() {
		return streamConfig;
	}

	public int getTaskId() {
		return task.getTaskId();
	}

	public int getStepCount() {
		return task.getStepCount();
	}

	public long getEncodingStartTime() {
		return task.getEncodingStartTime();
	}

	public long getEncodingEndTime() {
		return task.getEncodingEndTime();
	}

	public long getUnitCount() {
		return task.getUnitCount();
	}

	public Unit getUnit() {
		return task.getUnit();
	}

	public TaskProgress getProgress() {
		return task.getProgress();
	}

	public void setProgress(TaskProgress taskProgress) {
		task.setProgress(taskProgress);
	}

	public String getJobId() {
		return task.getJobId();
	}
}
