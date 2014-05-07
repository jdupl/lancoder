package drfoliberg.common.task;

import java.io.Serializable;

import drfoliberg.common.Status;

public class Task implements Serializable {

	private static final long serialVersionUID = -8705492902098705162L;
	protected TaskInfo taskInfo;
	protected TaskStatus taskStatus;

	public Task(int taskId, String sourceFile) {
		// TODO Auto-generated constructor stub
		taskInfo = new TaskInfo();
		taskInfo.setFileLocation(sourceFile);
		taskInfo.setTaskId(taskId);
		taskStatus = new TaskStatus();
	}

	public void setTaskStatus(TaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}

	public TaskStatus getTaskStatus() {
		return taskStatus;
	}

	public float getProgress() {
		return taskStatus.getProgress();
	}

	public void setProgress(float progress) {
		taskStatus.setProgress(progress);
	}

	public long getTimeElapsed() {
		return taskStatus.getTimeElapsed();
	}

	public void setTimeElapsed(long timeElapsed) {
		taskStatus.setTimeElapsed(timeElapsed);
	}

	public long getTimeEstimated() {
		return taskStatus.getTimeEstimated();
	}

	public void setTimeEstimated(long timeEstimated) {
		taskStatus.setTimeEstimated(timeEstimated);
	}

	public double getFps() {
		return taskStatus.getFps();
	}

	public void setFps(double fps) {
		taskStatus.setFps(fps);
	}

	public Status getStatus() {
		return taskStatus.getStatus();
	}

	public void setStatus(Status status) {
		taskStatus.setStatus(status);
	}

	public String getFileLocation() {
		return taskInfo.getFileLocation();
	}

	public void setFileLocation(String fileLocation) {
		taskInfo.setFileLocation(fileLocation);
	}

	public int getTaskId() {
		return taskInfo.getTaskId();
	}

	public void setTaskId(int taskId) {
		taskInfo.setTaskId(taskId);
	}

	public String getJobId() {
		return taskInfo.getJobId();
	}

	public void setJobId(String jobId) {
		taskInfo.setJobId(jobId);
	}

	public long getStartTime() {
		return taskInfo.getStartTime();
	}

	public void setStartTime(long startTime) {
		taskInfo.setStartTime(startTime);
	}

	public long getEndTime() {
		return taskInfo.getEndTime();
	}

	public void setEndTime(long endTime) {
		taskInfo.setEndTime(endTime);
	}

	public long getEstimatedFramesCount() {
		return taskInfo.getEstimatedFramesCount();
	}

	public void setEstimatedFramesCount(long estimatedFramesCount) {
		taskInfo.setEstimatedFramesCount(estimatedFramesCount);
	}

}
