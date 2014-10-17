package org.lancoder.master.api.node;

import java.net.InetAddress;

import org.lancoder.common.network.cluster.messages.ConnectMessage;
import org.lancoder.common.network.cluster.messages.StatusReport;

public interface MasterNodeServerListener {

	public String connectRequest(ConnectMessage cm, InetAddress detectedIp);

	public void disconnectRequest(ConnectMessage cm);

	public void readStatusReport(StatusReport report);

}
