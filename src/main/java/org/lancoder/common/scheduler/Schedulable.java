package org.lancoder.common.scheduler;

public abstract class Schedulable implements Comparable<Schedulable> {

	/**
	 * How many times to run. 0 is not limit
	 */
	protected int maxCount;

	protected long nextRun;
	protected long lastRun;
	protected int count;

	public abstract long getMsRunDelay();

	public abstract void runTask();

	public void scheduleNextRun() {
		if (maxCount == 0 || count < maxCount) {
			nextRun = System.currentTimeMillis() + getMsRunDelay();
		} else {
			nextRun = Long.MAX_VALUE;
		}
	}

	public final void runSchedule() {
		runTask();
		lastRun = System.currentTimeMillis();
		count++;

		scheduleNextRun();
	}

	@Override
	public int compareTo(Schedulable other) {
		return (int) (this.nextRun - other.nextRun);
	}

}
