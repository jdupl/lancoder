package org.lancoder.common.task;

import java.io.Serializable;

public abstract class ClientTask implements Serializable {

	private static final long serialVersionUID = 7072947025021592662L;
	protected Task task;
	protected StreamConfig streamConfig;

	public ClientTask(Task task, StreamConfig streamConfig) {
		this.task = task;
		this.streamConfig = streamConfig;
	}

	public void assign() {
		this.task.getProgress().assign();
	}

	public void cancel() {
		this.task.getProgress().cancel();
	}

	public void fail() {
		this.task.getProgress().fail();
	}

	public void start() {
		this.task.getProgress().start();
	}

	public void completed() {
		this.task.getProgress().complete();
	}

	public void reset() {
		this.task.getProgress().reset();
	}

	public abstract Task getTask();

	public String getTempFile() {
		return this.task.getTempFile();
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

	@Override
	public String toString() {
		return String.format("task %s from job %s", getTaskId(), getJobId());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ClientTask) {
			ClientTask other = (ClientTask) obj;
			return other.getJobId().equals(this.getJobId()) && other.getTaskId() == this.getTaskId();
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return task.getTaskId() + task.jobId.hashCode();
	}

}
