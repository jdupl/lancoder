package org.lancoder.common.network.messages.cluster;

import org.lancoder.common.network.messages.ClusterProtocol;

public class PingMessage extends Message {

	private static final long serialVersionUID = 1315837102845521312L;

	private PingMessage(ClusterProtocol code) {
		super(code);
	}

	public static PingMessage getPing() {
		return new PingMessage(ClusterProtocol.PING);
	}

	public static PingMessage getPong() {
		return new PingMessage(ClusterProtocol.PONG);
	}

}
