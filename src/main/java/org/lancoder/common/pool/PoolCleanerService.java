package org.lancoder.common.pool;

import java.util.ArrayList;

import org.lancoder.common.RunnableService;

public class PoolCleanerService extends RunnableService {

	private final static long CHECK_DELAY_MSEC = 1000 * 30;

	private ArrayList<Cleanable> cleanables;

	public PoolCleanerService(ArrayList<Cleanable> cleanables) {
		this.cleanables = cleanables;
	}

	@Override
	public void run() {
		while (!close) {
			try {
				for (Cleanable cleanable : cleanables) {
					if (cleanable.clean()) {
						System.out.printf("Cleaned %s%n", cleanable.getClass().getSimpleName());
					}
				}
				Thread.sleep(CHECK_DELAY_MSEC);
			} catch (InterruptedException e) {
				System.err.println("pool cleaner interrupted");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}

}