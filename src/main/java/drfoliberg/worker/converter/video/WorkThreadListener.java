package drfoliberg.worker.converter.video;

import drfoliberg.common.network.Cause;
import drfoliberg.common.task.Task;
import drfoliberg.worker.WorkerConfig;

public interface WorkThreadListener {

	public void workStarted(Task task);

	public void workCompleted(Task task);

	public void workFailed(Task task);

	public void nodeCrash(Cause cause);

	public WorkerConfig getConfig();
}