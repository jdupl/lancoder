package org.lancoder.master.api.node;

import org.lancoder.common.network.messages.cluster.ConnectMessage;
import org.lancoder.common.network.messages.cluster.StatusReport;

public interface MasterNodeServerListener {

	public String connectRequest(ConnectMessage cm);

	public void disconnectRequest(ConnectMessage cm);

	public void readStatusReport(StatusReport report);

}
