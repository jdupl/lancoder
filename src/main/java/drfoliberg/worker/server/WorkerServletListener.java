package drfoliberg.worker.server;

import drfoliberg.common.ServletListener;
import drfoliberg.common.network.messages.cluster.StatusReport;
import drfoliberg.common.task.Task;

public interface WorkerServletListener extends ServletListener {

	public boolean taskRequest(Task tqm);

	public boolean deleteTask(Task tqm);

	public StatusReport statusRequest();

	public void shutdownWorker();

}
