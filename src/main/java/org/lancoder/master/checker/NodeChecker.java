package org.lancoder.master.checker;

import java.io.IOException;

import org.lancoder.common.Node;
import org.lancoder.common.events.Event;
import org.lancoder.common.events.EventEnum;
import org.lancoder.common.events.EventListener;
import org.lancoder.common.network.MessageSender;
import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.pool.PoolWorker;

public class NodeChecker extends PoolWorker<Node> {

	private EventListener eventListener;

	public NodeChecker(EventListener eventListener) {
		this.eventListener = eventListener;
	}

	public void checkNode(Node n) {
		Message toSend = new Message(ClusterProtocol.STATUS_REQUEST);
		try {
			Message response = MessageSender.sendWithExceptions(toSend, n.getNodeAddress(), n.getNodePort());

			if (response instanceof StatusReport) {
				eventListener.handle(new Event((StatusReport) response));
			}
		} catch (IOException e) {
			eventListener.handle(new Event(EventEnum.NODE_DISCONNECTED, n));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void start() {
		checkNode(task);
	}
}
