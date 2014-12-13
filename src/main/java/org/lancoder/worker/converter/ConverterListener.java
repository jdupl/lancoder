package org.lancoder.worker.converter;

import org.lancoder.common.task.ClientTask;

public interface ConverterListener {

	public void taskCompleted(ClientTask task);

	public void taskStarted(ClientTask task);

	public void taskFailed(ClientTask task);
}
