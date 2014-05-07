package drfoliberg.common.task;

import java.io.Serializable;

public class TaskInfo implements Serializable {

	private static final long serialVersionUID = -7347337372025478193L;
	protected String sourceFile;
	protected int taskId;
	protected String jobId;
	protected long encodingStartTime;
	protected long encodingEndTime;
	protected long estimatedFramesCount;

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
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

	public long getEncodingStartTime() {
		return encodingStartTime;
	}

	public void setEncodingStartTime(long encodingStartTime) {
		this.encodingStartTime = encodingStartTime;
	}

	public long getEncodingEndTime() {
		return encodingEndTime;
	}

	public void setEncodingEndTime(long encodingEndTime) {
		this.encodingEndTime = encodingEndTime;
	}

	public long getEstimatedFramesCount() {
		return estimatedFramesCount;
	}

	public void setEstimatedFramesCount(long estimatedFramesCount) {
		this.estimatedFramesCount = estimatedFramesCount;
	}

}
