package drfoliberg.common.task;

import java.io.Serializable;

import drfoliberg.common.status.TaskState;

public class Task implements Serializable {

	public int getBitrate() {
		return taskInfo.getBitrate();
	}

	public void setBitrate(int bitrate) {
		taskInfo.setBitrate(bitrate);
	}

	private static final long serialVersionUID = -8705492902098705162L;
	protected TaskInfo taskInfo;
	protected TaskStatus taskStatus;

	public Task(int taskId, String sourceFile, int bitrate) {
		taskInfo = new TaskInfo();
		taskInfo.setSourceFile(sourceFile);
		taskInfo.setTaskId(taskId);
		taskInfo.setBitrate(bitrate);
		taskStatus = new TaskStatus();
	}

	public void reset() {
		taskStatus.setFramesCompleted(0);
		setStatus(TaskState.TASK_TODO);
	}

	public void start() {
		setTimeStarted(System.currentTimeMillis());
		setStatus(TaskState.TASK_COMPUTING);
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

	public TaskState getStatus() {
		return taskStatus.getState();
	}

	public void setStatus(TaskState status) {
		taskStatus.setStatus(status);
	}

	public String getSourceFile() {
		return taskInfo.getSourceFile();
	}

	public void setSourceFile(String sourceFile) {
		taskInfo.setSourceFile(sourceFile);
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

	public void setProgress(float progress) {
		taskStatus.setProgress(progress);
	}

	public void setTimeStarted(long timeStarted) {
		taskStatus.setTimeStarted(timeStarted);
	}

}
