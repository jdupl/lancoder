package org.lancoder.common.scheduler;

import org.lancoder.common.RunnableService;

public abstract class SchedulableService extends Schedulable implements RunnableService {

	protected volatile boolean close = false;

	public void stop() {
		this.close = true;
	}
}
