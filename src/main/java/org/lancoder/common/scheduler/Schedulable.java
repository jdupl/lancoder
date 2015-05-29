package org.lancoder.common.scheduler;


public abstract class Schedulable implements Comparable<Schedulable> {

	/**
	 * How many times to run. 0 is not limit
	 */
	protected int maxCount;

	protected long nextRun;
	protected long lastRun;
	protected int count;

	protected abstract long getMsRunDelay();

	protected abstract void runTask();

	protected void scheduleNextRun() {
		if (maxCount == 0 || count < maxCount) {
			nextRun = System.currentTimeMillis() + getMsRunDelay();
		} else {
			nextRun = Long.MAX_VALUE;
		}
	}

	protected final void runSchedule() {
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
