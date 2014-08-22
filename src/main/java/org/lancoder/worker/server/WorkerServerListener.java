package org.lancoder.worker.server;

import org.lancoder.common.network.messages.cluster.StatusReport;
import org.lancoder.common.task.Task;

public interface WorkerServerListener {

	public boolean taskRequest(Task tqm);

	public boolean deleteTask(Task tqm);

	public StatusReport statusRequest();

	public void shutdownWorker();

}
