package org.lancoder.common.network.cluster.messages;

import org.lancoder.common.network.cluster.protocol.ClusterProtocol;

public class ConnectResponse extends Message {

	private static final long serialVersionUID = 584883068943570814L;

	private String newUnid;
	private String webuiProtocol;
	private int webuiPort;

	public ConnectResponse(String newUnid, String webuiProtocol, int webuiPort) {
		super(ClusterProtocol.CONNECT_RESPONSE);
		this.newUnid = newUnid;
		this.webuiProtocol = webuiProtocol;
		this.webuiPort = webuiPort;
	}

	public String getNewUnid() {
		return newUnid;
	}

	public String getWebuiProtocol() {
		return webuiProtocol;
	}

	public int getWebuiPort() {
		return webuiPort;
	}

}
