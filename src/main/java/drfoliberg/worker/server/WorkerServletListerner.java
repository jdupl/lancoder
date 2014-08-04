package drfoliberg.worker.server;

import drfoliberg.common.ServletListener;
import drfoliberg.common.network.messages.cluster.StatusReport;
import drfoliberg.common.network.messages.cluster.TaskRequestMessage;

public interface WorkerServletListerner extends ServletListener {

	public boolean taskRequest(TaskRequestMessage tqm);

	public boolean deleteTask(TaskRequestMessage tqm);

	public StatusReport statusRequest();

	public void shutdownWorker();

}
