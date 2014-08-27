package org.lancoder.worker.server;

import org.lancoder.common.network.messages.cluster.StatusReport;
import org.lancoder.common.task.ClientTask;

public interface WorkerServerListener {

	public boolean taskRequest(ClientTask tqm);

	public boolean deleteTask(ClientTask tqm);

	public StatusReport statusRequest();

	public void shutdownWorker();

}
