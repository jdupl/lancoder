package org.lancoder.common.network.cluster.messages;

import java.util.ArrayList;

import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.status.NodeState;
import org.lancoder.common.task.TaskReport;

public class StatusReport extends AuthMessage {

	private static final long serialVersionUID = -844534455490561432L;
	private long loadAverage;
	private ArrayList<TaskReport> taskReports;
	public NodeState status;

	public StatusReport(NodeState status, String unid) {
		super(ClusterProtocol.STATUS_REPORT, unid);
		this.status = status;
	}

	public StatusReport(NodeState status, String unid, ArrayList<TaskReport> taskReports) {
		super(ClusterProtocol.STATUS_REPORT, unid);
		this.taskReports = taskReports;
		this.status = status;
	}

	public long getLoadAverage() {
		return loadAverage;
	}

	public void setLoadAverage(long loadAverage) {
		this.loadAverage = loadAverage;
	}

	public ArrayList<TaskReport> getTaskReports() {
		return taskReports;
	}

	public void setTaskReports(ArrayList<TaskReport> taskReports) {
		this.taskReports = taskReports;
	}

}
