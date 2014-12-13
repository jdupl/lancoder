package org.lancoder.common.network.cluster.messages;

import java.io.Serializable;

import org.lancoder.common.network.cluster.protocol.ClusterProtocol;

public class Message implements Serializable {

	private static final long serialVersionUID = -483657531000641905L;

	protected ClusterProtocol code;

	public Message(ClusterProtocol code) {
		this.code = code;
	}

	public ClusterProtocol getCode() {
		return code;
	}

}
