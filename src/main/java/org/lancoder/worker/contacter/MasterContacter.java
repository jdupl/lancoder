package org.lancoder.worker.contacter;

import java.io.IOException;
import java.net.InetAddress;

import org.lancoder.common.Service;
import org.lancoder.common.network.MessageSender;
import org.lancoder.common.network.cluster.messages.ConnectResponse;
import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.messages.PingMessage;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.scheduler.Schedulable;
import org.lancoder.common.status.NodeState;

public class MasterContacter extends Schedulable implements Service {

	private final static int DELAY_FAST_MSEC = 5000;
	private final static int DELAY_LONG_MSEC = 30000;

	protected volatile boolean close = false;
	private MasterContacterListener listener;
	private InetAddress masterAddress;
	private int masterPort;

	public MasterContacter(InetAddress masterAddress, int masterPort, MasterContacterListener listener) {
		this.listener = listener;
		this.masterAddress = masterAddress;
		this.masterPort = masterPort;
	}

	@Override
	public void stop() {
		this.close = true;
	}

	/**
	 * Get current message to master.
	 * 
	 * @return ConnectMessage if master is not connected. PingMessage otherwise.
	 */
	private Message getMessage() {
		return listener.getStatus() == NodeState.NOT_CONNECTED ? listener.getConnectMessage() : PingMessage.getPing();
	}

	private void contactMaster() {
		Message m = getMessage();

		try {
			Message response = MessageSender.sendWithExceptions(m, masterAddress, masterPort);
			switch (response.getCode()) {
			case CONNECT_RESPONSE:
				listener.onConnectResponse((ConnectResponse) response);
				break;
			case PONG:
				// Successful ping to master
				break;
			default:
				System.err.printf("Master sent invalid message %s%n", response.getClass().getSimpleName());
				break;
			}
		} catch (IOException e) {
			if (m.getCode() == ClusterProtocol.CONNECT_REQUEST) {
				System.err.println("Failed to contact master.");
			} else {
				listener.masterTimeout();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private int getNextDelay() {
		return listener.getStatus() == NodeState.NOT_CONNECTED ? DELAY_FAST_MSEC : DELAY_LONG_MSEC;
	}

	@Override
	protected long getMsRunDelay() {
		return getNextDelay();
	}

	@Override
	protected void runTask() {
		contactMaster();
	}

}
