package drfoliberg.network;

import drfoliberg.NodeReport;

public class StatusReport extends Message{
	
	private static final long serialVersionUID = -844534455490561432L;
	private NodeReport report;

	public StatusReport(){
		super(ClusterProtocol.STATUS_REPORT);
	}
}
