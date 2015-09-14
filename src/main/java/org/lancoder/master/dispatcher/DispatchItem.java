package org.lancoder.master.dispatcher;

import org.lancoder.common.Node;
import org.lancoder.common.network.cluster.messages.Message;

public class DispatchItem {

	private Message message;
	private Node node;

	public DispatchItem(Message message, Node node) {
		this.message = message;
		this.node = node;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof DispatchItem) {
			DispatchItem other = (DispatchItem) obj;
			return other.getNode().equals(this.getNode()) && other.getMessage().equals(this.getMessage());
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return "DispatchItem [message=" + message + ", node=" + node + "]";
	}

}
