package drfoliberg.common.task;

import java.io.Serializable;

import drfoliberg.common.Status;

public class Task implements Serializable {

	private static final long serialVersionUID = 7435087680360843645L;
	protected String fileLocation;
	protected int taskId;
	protected String jobId;
	protected Status status;
	protected double progress;
	
	public Task(int taskId, String fileLocation) {
		this.taskId = taskId;
		this.fileLocation = fileLocation;
		this.status = Status.JOB_TODO;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}
}
