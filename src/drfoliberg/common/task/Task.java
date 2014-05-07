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

	public void reset() {
		taskStatus.setFramesCompleted(0);
		setStatus(Status.JOB_TODO);
	}

	public void start() {
		setTimeStarted(System.currentTimeMillis());
		setStatus(Status.JOB_COMPUTING);
	}

	public long getETA() {
		long elapsedMs = System.currentTimeMillis() - getTimeStarted();
		return (long) (elapsedMs / (getProgress() / 100));
	}

	public float getProgress() {
		float percentToComplete = ((float) taskStatus.getFramesCompleted() / taskInfo.getEstimatedFramesCount()) * 100;
		return percentToComplete;
	}

	public long getFramesCompleted() {
		return taskStatus.getFramesCompleted();
	}

	public void setFramesCompleted(long framesCompleted) {
		taskStatus.setFramesCompleted(framesCompleted);
	}

	public void setTaskStatus(TaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}

	public TaskStatus getTaskStatus() {
		return taskStatus;
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

	public long getEncodingStartTime() {
		return taskInfo.getEncodingStartTime();
	}

	public void setEncodingStartTime(long encodingStartTime) {
		taskInfo.setEncodingStartTime(encodingStartTime);
	}

	public long getEncodingEndTime() {
		return taskInfo.getEncodingEndTime();
	}

	public void setEncodingEndTime(long encodingEndTime) {
		taskInfo.setEncodingEndTime(encodingEndTime);
	}

	public long getEstimatedFramesCount() {
		return taskInfo.getEstimatedFramesCount();
	}

	public void setEstimatedFramesCount(long estimatedFramesCount) {
		taskInfo.setEstimatedFramesCount(estimatedFramesCount);
	}

	public long getTimeStarted() {
		return taskStatus.getTimeStarted();
	}

	public void setTimeStarted(long timeStarted) {
		taskStatus.setTimeStarted(timeStarted);
	}

}
