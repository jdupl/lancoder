package org.lancoder.master.checker;

import java.util.ArrayList;

import org.lancoder.common.Node;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.task.TaskReport;

public interface NodeCheckerListener {
	
	public void nodeDisconnected(Node n);

	public void readTaskReports(ArrayList<TaskReport> taskReports);

	public boolean readStatusReport(StatusReport statusReport);
}
