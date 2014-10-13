package org.lancoder.master.dispatcher;

public interface DispatcherListener {

	public void taskAccepted(DispatchItem item);
	
	public void taskRefused(DispatchItem item);
}
