package drfoliberg.common.task;

import java.io.Serializable;

import drfoliberg.common.job.JobConfig;
import drfoliberg.common.job.RateControlType;
import drfoliberg.common.status.TaskState;

public class Task implements Serializable {

	private static final long serialVersionUID = -8705492902098705162L;
	protected TaskInfo taskInfo;
	protected TaskStatus taskStatus;

	public Task(int taskId, JobConfig config) {
		taskInfo = new TaskInfo(config);
		// taskInfo.setSourceFile(sourceFile);
		taskInfo.setTaskId(taskId);
		// taskInfo.setRate(rate);
		taskStatus = new TaskStatus();
	}
	
	public String getRateControlArg() {
		switch (taskInfo.getRateControlType()) {
		case VBR:
			return String.format("-b:v %dk", this.getRate());
		case CRF:
			return String.format("-crf %d", this.getRate());
		default:
			// TODO throw exception
			break;
		}
		return null;
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
	
	public RateControlType getRateControlType() {
		return taskInfo.getRateControlType();
	}

	public void setRateControlType(RateControlType rateControlType) {
		taskInfo.setRateControlType(rateControlType);
	}

	public int getRate() {
		return taskInfo.getRate();
	}

	public void setRate(int rate) {
		taskInfo.setRate(rate);
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
