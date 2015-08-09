package org.lancoder.common.task;

import java.io.Serializable;

import org.lancoder.common.math.average.timed.TimedMovingAverage;
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

	protected TimedMovingAverage average = new TimedMovingAverage(60 * 1000);

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
	public synchronized void update(long units) {
		long unitsSinceLast = units - unitsCompleted;
		long msElapsed = System.currentTimeMillis() - this.lastUpdate;
		updateSpeed(unitsSinceLast, msElapsed);
		updateProgress(units);
	}

	/**
	 * Get speed estimation in units/second and update progress speed.
	 * 
	 * @param units
	 *            The units since last update
	 * @param ms
	 *            The msec elapsed since last update
	 * @return if update was successful (valid speed estimate)
	 */
	private boolean updateSpeed(double units, long ms) {
		boolean updated = true;
		double estimatedSpeed = units / ms * 1000;

		if (!Double.isInfinite(estimatedSpeed) && !Double.isNaN(estimatedSpeed)) {
			this.average.add(estimatedSpeed, System.currentTimeMillis());
			this.speed = average.getAverage();
		} else {
			updated = false;
		}

		return updated;
	}

	/**
	 * Update progress with the unit count and estimated time remaining.
	 * 
	 * @param units
	 *            The current count of units completed
	 */
	private void updateProgress(long units) {
		this.lastUpdate = System.currentTimeMillis();
		this.timeElapsed = lastUpdate - this.timeStarted;
		this.unitsCompleted = units;
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
	public synchronized void reset() {
		this.taskState = TaskState.TASK_TODO;
		this.progress = 0;
		this.lastUpdate = 0;
		this.timeElapsed = 0;
		this.timeEstimated = 0;
		this.timeStarted = 0;
		this.unitsCompleted = 0;
		this.average.clear();
	}

	public synchronized void complete() {
		this.taskState = TaskState.TASK_COMPLETED;
		this.progress = 100.0;
		this.lastUpdate = System.currentTimeMillis();
		this.timeElapsed = lastUpdate - this.timeStarted;
		this.timeEstimated = 0;
		this.speed = 0;
		this.unitsCompleted = unitsTotal;
		this.average.clear();
	}
}
