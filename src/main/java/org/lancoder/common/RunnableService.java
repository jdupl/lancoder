package org.lancoder.common;

public abstract class RunnableService extends Service implements Runnable {

	/**
	 * Service unexpectedly closed because of fatal exception.
	 * 
	 * @param e
	 */
	public abstract void serviceFailure(Exception e);

}