package org.lancoder.common.task;

import java.io.File;
import java.io.Serializable;

public class Task implements Serializable {

	private static final long serialVersionUID = 6687973244041343482L;
	protected int taskId;
	protected String jobId;
	protected int stepCount;
	protected long encodingStartTime;
	protected long encodingEndTime;
	protected long unitCount;
	protected Unit unit;
	protected TaskProgress taskProgress;
	/**
	 * Path to the temporary file located on the worker. Relative to the tmp folder.
	 */
	protected File tempFile;
	/**
	 * Path to the temporary file located on the shared folder. This 'final' step is only the encoding part. Muxing will
	 * delete this file.
	 */
	protected File finalFile;

	public Task(int taskId, String jobId, int stepCount, long encodingStartTime, long encodingEndTime, long unitCount,
			Unit unit, File tempFile, File finalFile) {
		this.taskId = taskId;
		this.jobId = jobId;
		this.stepCount = stepCount;
		this.encodingStartTime = encodingStartTime;
		this.encodingEndTime = encodingEndTime;
		this.unitCount = unitCount;
		this.unit = unit;
		this.taskProgress = new TaskProgress(unitCount, stepCount, unit);
		this.tempFile = tempFile;
		this.finalFile = finalFile;
	}

	public File getFinalFile() {
		return finalFile;
	}

	public File getTempFile() {
		return tempFile;
	}

	public int getTaskId() {
		return taskId;
	}

	public int getStepCount() {
		return stepCount;
	}

	public long getEncodingStartTime() {
		return encodingStartTime;
	}

	public long getEncodingEndTime() {
		return encodingEndTime;
	}

	public long getUnitCount() {
		return unitCount;
	}

	public Unit getUnit() {
		return unit;
	}

	public TaskProgress getProgress() {
		return taskProgress;
	}

	public void setProgress(TaskProgress taskProgress) {
		this.taskProgress = taskProgress;
	}

	public String getJobId() {
		return this.jobId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Task) {
			Task other = (Task) obj;
			return other.taskId == this.taskId && other.jobId.equals(this.jobId);
		}
		return super.equals(obj);
	}
}
