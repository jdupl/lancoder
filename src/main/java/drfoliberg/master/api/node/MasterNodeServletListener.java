package main.java.drfoliberg.master.api.node;

import main.java.drfoliberg.common.ServletListener;
import main.java.drfoliberg.common.network.messages.cluster.ConnectMessage;
import main.java.drfoliberg.common.network.messages.cluster.StatusReport;

public interface MasterNodeServletListener extends ServletListener {

	public String readConnectRequest(ConnectMessage cm);

	public void readStatusReport(StatusReport report);

}
