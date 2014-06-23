package drfoliberg.common.task.video;

import drfoliberg.common.job.JobConfig;

public class TaskInfo extends JobConfig {

	private static final long serialVersionUID = -7347337372025478193L;
	protected int taskId;
	protected String jobId;
	/**
	 * Path of the file relative to the final encodes output folder.
	 */
	protected String outputFile;

	protected long encodingStartTime;
	protected long encodingEndTime;
	protected long estimatedFramesCount;

	public TaskInfo(JobConfig config) {
		super(config);
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

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

}
