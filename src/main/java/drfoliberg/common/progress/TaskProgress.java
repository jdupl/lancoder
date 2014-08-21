package drfoliberg.common.progress;

import java.io.Serializable;
import java.util.LinkedHashMap;

import drfoliberg.common.status.TaskState;

public class TaskProgress implements Serializable {

	private static final long serialVersionUID = 7437966237627538221L;

	private int currentPassIndex = 1;
	private LinkedHashMap<Integer, Progress> steps = new LinkedHashMap<>();
	private TaskState taskState = TaskState.TASK_TODO;

	public TaskProgress(long units, int steps) {
		for (int i = 1; i <= steps; i++) {
			this.steps.put(i, new Progress(units));
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
	 * Update current task to specified units.
	 * 
	 * @param units
	 *            The unit count currently completed
	 */
	public void update(long units) {
		this.getCurrentStep().update(units);
	}

	/**
	 * Update current task to specified units and speed.
	 * 
	 * @param units
	 *            The unit count currently completed
	 * 
	 * @param speed
	 *            The speed in units / second
	 */
	public void update(long units, double speed) {
		this.getCurrentStep().update(units, speed);
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

	public int getCurrentPassIndex() {
		return currentPassIndex;
	}

	public TaskState getTaskState() {
		return this.taskState;
	}
}
