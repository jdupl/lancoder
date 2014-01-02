package drfoliberg.common.network;

import drfoliberg.common.Node;

public class StatusReport extends Message {

	private static final long serialVersionUID = -844534455490561432L;

	public StatusReport(Node n) {
		super(ClusterProtocol.STATUS_REPORT);
		this.node = n;
	}
}
