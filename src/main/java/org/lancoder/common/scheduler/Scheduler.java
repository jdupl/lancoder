package org.lancoder.common.scheduler;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lancoder.common.RunnableServiceAdapter;

public class Scheduler extends RunnableServiceAdapter {

	private ConcurrentSkipListSet<Schedulable> schedulables = new ConcurrentSkipListSet<>();
	private Thread schedulerThread;
	private long sleepUntil;
	private AtomicBoolean sleeping = new AtomicBoolean(false);

	public synchronized void addSchedulable(Schedulable schedulable) {
		if (schedulable.runAsapOnScheduler()) {
			schedulable.scheduleNow();
		} else {
			schedulable.scheduleNextRun();
		}

		schedulables.add(schedulable);
		refresh();
	}

	public void setThread(Thread t) {
		this.schedulerThread = t;
	}

	private void refresh() {
		if (!schedulables.isEmpty()) {
			Schedulable next = schedulables.first();
			sleepUntil = next.nextRun;

			if (sleeping.get()) {
				this.schedulerThread.interrupt();
			}
		} else {
			sleepUntil = System.currentTimeMillis() + 10000;
		}
	}

	@Override
	public void run() {
		sleepUntil = System.currentTimeMillis() + 100;

		while (!close) {
			try {
				sleepUntil(sleepUntil);

				if (schedulables.size() > 0) {
					Schedulable next = schedulables.pollFirst();
					next.runSchedule();
					schedulables.add(next);
				}
			} catch (InterruptedException e) {
				sleeping.set(false);
			} finally {
				refresh();
			}
		}
	}

	private void sleepUntil(long sleepUntil) throws InterruptedException {
		long currentMs = System.currentTimeMillis();

		if (currentMs < sleepUntil) {
			sleeping.set(true);
			Thread.sleep(sleepUntil - currentMs);
			sleeping.set(false);
		}
	}
}
