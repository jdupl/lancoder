package org.lancoder.common.scheduler;

import java.util.concurrent.PriorityBlockingQueue;

import org.lancoder.common.RunnableService;

public class Scheduler extends RunnableService {

	private PriorityBlockingQueue<Schedulable> schedulables = new PriorityBlockingQueue<>();
	private Thread schedulerThread;

	private long sleepUntil;
	private boolean sleeping = false;

	public synchronized void addSchedulable(Schedulable schedulable) {
		this.schedulables.add(schedulable);
		refresh();
	}

	public void setThread(Thread t) {
		this.schedulerThread = t;
	}

	private void refresh() {
		if (!schedulables.isEmpty()) {
			Schedulable next = schedulables.peek();
			sleepUntil = next.nextRun;
			if (sleeping) {
				this.schedulerThread.interrupt();
			}
		}
	}

	@Override
	public void run() {
		sleepUntil = Long.MAX_VALUE;

		while (!close) {
			try {
				sleepUntil(sleepUntil);

				if (schedulables.size() > 0) {
					Schedulable next = schedulables.poll();
					next.runSchedule();
				}
			} catch (InterruptedException e) {
				sleeping = false;
			} finally {
				refresh();
			}
		}
	}

	private void sleepUntil(long sleepUntil) throws InterruptedException {
		sleeping = true;
		long currentMs = System.currentTimeMillis();

		if (currentMs < sleepUntil) {
			Thread.sleep(sleepUntil - currentMs);
		}
		sleeping = false;
	}

	@Override
	public void serviceFailure(Exception e) {

	}
}
