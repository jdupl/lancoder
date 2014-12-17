package org.lancoder.common.task;

import java.io.Serializable;

import org.lancoder.common.progress.TaskProgress;
import org.lancoder.common.progress.Unit;

public abstract class ClientTask implements Serializable {

	private static final long serialVersionUID = 7072947025021592662L;
	protected Task task;
	protected StreamConfig streamConfig;

	public ClientTask(Task task, StreamConfig streamConfig) {
		this.task = task;
		this.streamConfig = streamConfig;
	}

	public void cancel() {
		this.task.getProgress().cancel();
	}

	public abstract Task getTask();

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

	public String getTempFile() {
		return this.task.getTempFile();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ClientTask) {
			ClientTask other = (ClientTask) obj;
			return other.getJobId().equals(this.getJobId()) && other.getTaskId() == this.getTaskId();
		}
		return super.equals(obj);
	}
}
