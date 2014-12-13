package org.lancoder.common.network.cluster.messages;

import org.lancoder.common.Node;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;

public class ConnectRequest extends AuthMessage {

	private static final long serialVersionUID = 831513295350691753L;

	private Node node;

	public ConnectRequest(Node node) {
		super(ClusterProtocol.CONNECT_REQUEST, node.getUnid());
		this.node = node;
	}

	public Node getNode() {
		return node;
	}
}
