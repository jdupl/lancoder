package drfoliberg.common.network;

import drfoliberg.common.Node;

public class StatusReport extends Message {

	private static final long serialVersionUID = -844534455490561432L;
	private long loadAverage;
	private TaskReport taskReport;

	public StatusReport(Node n) {
		super(ClusterProtocol.STATUS_REPORT);
		this.node = n;
	}

	public StatusReport(Node n, TaskReport taskReport) {
		super(ClusterProtocol.STATUS_REPORT);
		this.node = n;
		this.taskReport = taskReport;
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

}
