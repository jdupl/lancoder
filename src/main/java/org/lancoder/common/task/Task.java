package org.lancoder.common.task;

import java.io.Serializable;

import org.lancoder.common.progress.TaskProgress;
import org.lancoder.common.progress.Unit;

public class Task implements Serializable {

	private static final long serialVersionUID = 6687973244041343482L;
	protected int taskId;
	protected int stepCount;
	protected long encodingStartTime;
	protected long encodingEndTime;
	protected long unitCount;
	protected Unit unit;
	protected TaskProgress taskProgress;

	public Task(int taskId, int stepCount, long encodingStartTime, long encodingEndTime, long unitCount,
			Unit unit) {
		this.taskId = taskId;
		this.stepCount = stepCount;
		this.encodingStartTime = encodingStartTime;
		this.encodingEndTime = encodingEndTime;
		this.unitCount = unitCount;
		this.unit = unit;
		this.taskProgress = new TaskProgress(unitCount, stepCount, unit);
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
		// TODO add the job id reference to object
		return null;
	}
}
