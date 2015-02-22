package org.lancoder.common.network.cluster.messages;

import org.lancoder.common.network.cluster.protocol.ClusterProtocol;


public class PingMessage extends Message {

	private static final long serialVersionUID = 1315837102845521312L;

	private PingMessage(ClusterProtocol code) {
		super(code);
	}

		return new PingMessage(ClusterProtocol.PING);
	}
         	public static PingMessage getPong() {
		return new PingMessage(ClusterProtocol.PONG);
	}

}
