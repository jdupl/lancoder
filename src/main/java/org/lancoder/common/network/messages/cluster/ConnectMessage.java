package org.lancoder.common.network.messages.cluster;

import org.lancoder.common.Node;
import org.lancoder.common.network.messages.ClusterProtocol;

public class ConnectMessage extends AuthMessage {

	private static final long serialVersionUID = 831513295350691753L;

	private Node node;

	public ConnectMessage(Node node) {
		super(ClusterProtocol.CONNECT_ME, node.getUnid());
		this.node = node;
	}

	public Node getNode() {
		return node;
	}
}
