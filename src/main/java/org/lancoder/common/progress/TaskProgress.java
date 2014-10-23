package org.lancoder.common.progress;

import java.io.Serializable;
import java.util.LinkedHashMap;

import org.lancoder.common.status.TaskState;

public class TaskProgress implements Serializable {

	private static final long serialVersionUID = 7437966237627538221L;

	private int currentPassIndex = 1;
	private LinkedHashMap<Integer, Progress> steps = new LinkedHashMap<>();
	private TaskState taskState = TaskState.TASK_TODO;

	public TaskProgress(long units, int steps, Unit unit) {
		for (int i = 1; i <= steps; i++) {
			this.steps.put(i, new Progress(units, unit));
		}
	}

	public Progress getCurrentStep() {
		return this.steps.get(currentPassIndex);
	}

	public void start() {
		this.taskState = TaskState.TASK_COMPUTING;
		this.getCurrentStep().start();
	}

	/**
	 * Update current task to specified units. Will update progress by estimating speed.
	 * 
	 * @param units
	 *            The unit count currently completed
	 */
	public void update(long units) {
		this.getCurrentStep().update(units);
	}

	public void reset() {
		this.taskState = TaskState.TASK_TODO;
		this.currentPassIndex = 1;
		for (int i = 1; i < steps.size(); i++) {
			steps.get(i).reset();
		}
	}

	public void completeStep() {
		this.getCurrentStep().complete();
		if (this.currentPassIndex < this.steps.size()) {
			this.currentPassIndex++;
			this.getCurrentStep().start();
		}
	}

	public void complete() {
		this.taskState = TaskState.TASK_COMPLETED;
	}

	public int getCurrentStepIndex() {
		return currentPassIndex;
	}

	public TaskState getTaskState() {
		return this.taskState;
	}
}
