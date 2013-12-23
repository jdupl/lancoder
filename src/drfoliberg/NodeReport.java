package drfoliberg;

import drfoliberg.worker.Worker;

public class NodeReport {

	private int status;
	private String name;
	private int listenPort;

	public NodeReport(Worker n) {
		this.listenPort = n.getListenPort();
		this.status = n.getStatus();
		this.name = n.getName();
	}

	public int getStatus() {
		return status;
	}

	public String getName() {
		return name;
	}

	public int getListenPort() {
		return listenPort;
	}

}
