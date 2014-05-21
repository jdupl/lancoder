package drfoliberg.common.network.messages.cluster;

import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.status.NodeState;
import drfoliberg.common.task.TaskReport;

public class StatusReport extends AuthMessage {

	private static final long serialVersionUID = -844534455490561432L;
	private long loadAverage;
	private TaskReport taskReport;
	//public Node node;
	public NodeState status;

//	public StatusReport(Node n) {
//		super(ClusterProtocol.STATUS_REPORT);
//		this.node = n;
//	}
	public StatusReport(NodeState status, String unid) {
		super(ClusterProtocol.STATUS_REPORT, unid);
		this.status = status;
	}

	public StatusReport(NodeState status, String unid, TaskReport taskReport) {
		super(ClusterProtocol.STATUS_REPORT, unid);
		this.taskReport = taskReport;
		this.status = status;
	}

	public long getLoadAverage() {
		return loadAverage;
	}

	public void setLoadAverage(long loadAverage) {
		this.loadAverage = loadAverage;
	}

	public TaskReport getTaskReport() {
		return taskReport;
	}

    public void setTaskReport(TaskReport taskReport) {
        this.taskReport = taskReport;
    }
}
