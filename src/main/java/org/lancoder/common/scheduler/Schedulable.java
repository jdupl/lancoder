package org.lancoder.common.scheduler;

public abstract class Schedulable implements Comparable<Schedulable> {

	/**
	 * At what interval to run
	 */
	protected long intervalMSec = 1000;
	/**
	 * How many times to run. 0 is not limit
	 */
	protected int maxCount;

	protected long nextRun;
	protected long lastRun;
	protected int count;

	public final void runSchedule() {
		lastRun = System.currentTimeMillis();
		runTask();
		count++;

		if (maxCount == 0 || count < maxCount) {
			nextRun = lastRun + intervalMSec;
		}
	}

	public abstract void runTask();

	@Override
	public int compareTo(Schedulable other) {
		return (int) (this.nextRun - other.nextRun);
	}

}
