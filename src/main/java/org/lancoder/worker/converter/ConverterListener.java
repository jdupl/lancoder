package org.lancoder.worker.converter;

import org.lancoder.common.network.Cause;
import org.lancoder.common.task.Task;
import org.lancoder.worker.WorkerConfig;

/**
 * TODO Refactor to abstract class and inherit service class ?
 *
 */
public interface ConverterListener {

	public void workStarted(Task task);

	public void workCompleted(Task task);

	public void workFailed(Task task);

	public void nodeCrash(Cause cause);

	public WorkerConfig getConfig();
}