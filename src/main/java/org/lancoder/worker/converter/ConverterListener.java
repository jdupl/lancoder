package org.lancoder.worker.converter;

import org.lancoder.common.network.Cause;
import org.lancoder.common.task.ClientTask;
import org.lancoder.worker.WorkerConfig;

/**
 * TODO Refactor to abstract class and inherit service class ?
 *
 */
public interface ConverterListener {

	public void workStarted(ClientTask task);

	public void workCompleted(ClientTask task);

	public void workFailed(ClientTask task);

	public void nodeCrash(Cause cause);

	public WorkerConfig getConfig();
}