package drfoliberg.common.task;

import java.io.Serializable;

public class TaskInfo implements Serializable {

	private static final long serialVersionUID = -7347337372025478193L;
	protected String fileLocation;
	protected int taskId;
	protected String jobId;
	protected long startTime;
	protected long endTime;
	protected long estimatedFramesCount;

	public String getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
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

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getEstimatedFramesCount() {
		return estimatedFramesCount;
	}

	public void setEstimatedFramesCount(long estimatedFramesCount) {
		this.estimatedFramesCount = estimatedFramesCount;
	}

}
