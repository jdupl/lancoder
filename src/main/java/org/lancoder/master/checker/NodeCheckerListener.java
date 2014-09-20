package org.lancoder.master.checker;

import java.util.ArrayList;

import org.lancoder.common.Node;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.task.TaskReport;

public interface NodeCheckerListener {

	public ArrayList<Node> getNodes();

	public ArrayList<Node> getOnlineNodes();
	
	public void nodeDisconnected(Node n);

	public void readTaskReports(ArrayList<TaskReport> taskReports);

	public void readStatusReport(StatusReport statusReport);
}
