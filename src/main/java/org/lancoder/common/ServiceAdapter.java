package org.lancoder.common;

public abstract class ServiceAdapter implements Service {

	protected volatile boolean close;

	@Override
	public void stop() {
		this.close = true;
	}
}