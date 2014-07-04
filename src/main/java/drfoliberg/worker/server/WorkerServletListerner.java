package main.java.drfoliberg.worker.server;

import main.java.drfoliberg.common.ServletListener;
import main.java.drfoliberg.common.network.messages.cluster.StatusReport;
import main.java.drfoliberg.common.network.messages.cluster.TaskRequestMessage;

public interface WorkerServletListerner extends ServletListener {

	public boolean taskRequest(TaskRequestMessage tqm);

	public boolean deleteTask(TaskRequestMessage tqm);

	public StatusReport statusRequest();

	public void shutdownWorker();

}
