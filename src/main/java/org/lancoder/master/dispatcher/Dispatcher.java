package org.lancoder.master.dispatcher;

import org.lancoder.common.Node;
import org.lancoder.common.network.MessageSender;
import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.pool.PoolWorker;

public class Dispatcher extends PoolWorker<DispatchItem> {

	private DispatcherListener listener;

	public Dispatcher(DispatcherListener listener) {
		this.listener = listener;
	}

	private void dispatch(DispatchItem item) {
		Node node = item.getNode();

		Message response = MessageSender.send(item.getMessage(), node.getNodeAddress(), node.getNodePort());
		if (response != null && response.getCode() == ClusterProtocol.OK) {
			// dispatching was successful
			listener.taskAccepted(item);
		} else {
			// timeout or internal error
			listener.taskRefused(item);
		}
	}

	@Override
	protected void start() {
		dispatch(task);
	}
}
