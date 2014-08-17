package drfoliberg.common.progress;

import java.io.Serializable;
import java.util.ArrayList;

import drfoliberg.common.status.TaskState;

public class TaskProgress implements Serializable {

	private static final long serialVersionUID = 7437966237627538221L;
	protected long timeStarted;
	protected long timeElapsed;
	protected long timeEstimated;

	protected long unitsCompleted;
	protected long unitsTotal;
	protected double speed;

	protected int currentPass;
	/**
	 * Progress is always calculated in Task object itself. We still need this field here for serialization.
	 */
	protected double progress;
	protected TaskState taskState = TaskState.TASK_TODO;
	protected ArrayList<Step> steps = new ArrayList<>();

	public TaskProgress(long units, int steps) {
		this.unitsTotal = units;
		for (int i = 1; i <= steps; i++) {
			this.steps.add(new Step(units));
		}
	}

	public void update(long units, int step) {

	}

	public void start() {

	}

	public void reset() {

	}

	public void complete() {

	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

	public long getTimeStarted() {
		return timeStarted;
	}

	public void setTimeStarted(long timeStarted) {
		this.timeStarted = timeStarted;
	}

	public long getFramesCompleted() {
		return unitsCompleted;
	}

	public void setFramesCompleted(long framesCompleted) {
		this.unitsCompleted = framesCompleted;
	}

	public long getTimeElapsed() {
		return timeElapsed;
	}

	public void setTimeElapsed(long timeElapsed) {
		this.timeElapsed = timeElapsed;
	}

	public long getTimeEstimated() {
		return timeEstimated;
	}

	public void setTimeEstimated(long timeEstimated) {
		this.timeEstimated = timeEstimated;
	}

	public double getFps() {
		return speed;
	}

	public void setFps(double fps) {
		this.speed = fps;
	}

	public int getCurrentPass() {
		return currentPass;
	}

	public void setCurrentPass(int currentPass) {
		this.currentPass = currentPass;
	}

	public TaskState getTaskState() {
		return this.taskState;
	}

	public void setTaskState(TaskState taskState) {
		this.taskState = taskState;
	}
}
