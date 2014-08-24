package org.lancoder.common.progress;

import java.io.Serializable;

import org.lancoder.common.status.TaskState;

public class Progress implements Serializable {

	private static final long serialVersionUID = -671719004619100038L;
	/**
	 * Total count of units
	 */
	protected long unitsTotal;
	/**
	 * Count of units that are currently completed
	 */
	protected long unitsCompleted;
	/**
	 * The kind of unit (used for webui)
	 */
	protected Unit unit;
	/**
	 * Speed in units/sec
	 */
	protected double speed;
	/**
	 * The time in ms where the task/step was started.
	 */
	protected long timeStarted;
	/**
	 * The total time elapsed since the start of the task/step.
	 */
	protected long timeElapsed;
	/**
	 * An estimate of the time remaining io ms. Calculated from current speed and units remaining.
	 */
	protected long timeEstimated;
	/**
	 * The % progress
	 */
	protected double progress;
	/**
	 * Current state of the task/step
	 */
	protected TaskState taskState = TaskState.TASK_TODO;
	/**
	 * The time in ms of the last update
	 */
	protected long lastUpdate;

	public Progress(long units, Unit unit) {
		this.unitsTotal = units;
		this.unit = unit;
	}

	/**
	 * Update progress and estimate speed with the units completed since the last update.
	 * 
	 * @param units
	 *            The current count of units completed
	 */
	public void update(long units) {
		double unitsSinceLast = units - unitsCompleted;
		long msElapsed = System.currentTimeMillis() - this.lastUpdate;
		double estimatedSpeed = unitsSinceLast / msElapsed * 1000.0;
		if (!Double.isInfinite(estimatedSpeed)) {
			update(units, estimatedSpeed);
		}
	}

	/**
	 * Update progress with the unit count and speed.
	 * 
	 * @param units
	 *            The current count of units completed
	 * @param speed
	 *            The current speed
	 */
	public void update(long units, double speed) {
		this.lastUpdate = System.currentTimeMillis();
		this.timeElapsed = lastUpdate - this.timeStarted;
		this.unitsCompleted = units;
		this.speed = speed;
		long remainingUnits = unitsTotal - unitsCompleted;
		this.timeEstimated = ((long) (remainingUnits / this.speed)) * 1000;
		this.progress = (unitsCompleted * 100.0) / unitsTotal;
	}

	public void start() {
		this.reset();
		this.taskState = TaskState.TASK_COMPUTING;
		this.timeStarted = System.currentTimeMillis();
		this.lastUpdate = timeStarted;
	}

	/**
	 * Reset the progress but keep the count of total units
	 */
	public void reset() {
		this.taskState = TaskState.TASK_TODO;
		this.progress = 0;
		this.lastUpdate = 0;
		this.timeElapsed = 0;
		this.timeEstimated = 0;
		this.timeStarted = 0;
		this.unitsCompleted = 0;
	}

	public void complete() {
		this.taskState = TaskState.TASK_COMPLETED;
		this.progress = 100.0;
		this.lastUpdate = System.currentTimeMillis();
		this.timeElapsed = lastUpdate - this.timeStarted;
		this.timeEstimated = 0;
		this.speed = 0;
		this.unitsCompleted = unitsTotal;
	}
}
