package org.lancoder.master.api.node;

import org.lancoder.common.network.cluster.messages.ConnectMessage;
import org.lancoder.common.network.cluster.messages.StatusReport;

public interface MasterNodeServerListener {

	public String connectRequest(ConnectMessage cm);

	public void disconnectRequest(ConnectMessage cm);

	public void readStatusReport(StatusReport report);

}
