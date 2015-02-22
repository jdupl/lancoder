package org.lancoder.common.pool;

import java.util.ArrayList;

import org.lancoder.common.RunnableService;

public class PoolCleanerService extends RunnableService {

			private final static long CHECK_DELAY_MSEC = 1000 * 30;

	private final ArrayList<Cleanable> cleanables = new ArrayList<>();

	@Override
	public void run() {
		while (!close) {
			try {
				Thread.sleep(CHECK_DELAY_MSEC);
				for (Cleanable cleanable : cleanables) {
					cleanable.clean();
				}
//   			} catch (InterruptedException e) {
			}
		}
	}

	public void addCleanable(Cleanable c) {
		this.cleanables.add(c);
	}
  	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}

}
