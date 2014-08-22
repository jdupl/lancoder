package org.lancoder.common.network.messages.cluster;

import java.io.Serializable;

import org.lancoder.common.network.messages.ClusterProtocol;

public class Message implements Serializable {

	private static final long serialVersionUID = -483657531000641905L;

	@Deprecated
	protected String path;
	protected ClusterProtocol code;

	@Deprecated
	public Message(String path) {
		this.path = path;
	}

	public Message(ClusterProtocol code) {
		this.code = code;
	}

	public String getPath() {
		return path;
	}

	public ClusterProtocol getCode() {
		return code;
	}

}
