package org.lancoder.worker.contacter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.lancoder.common.RunnableService;
import org.lancoder.common.network.cluster.messages.ConnectResponse;
import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.messages.PingMessage;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.status.NodeState;

public class MasterContacter extends RunnableService {

	private final static int DELAY_FAST_MSEC = 5000;
	private final static int DELAY_LONG_MSEC = 30000;

	private MasterContacterListener listener;
	private InetAddress masterAddress;
	private int masterPort;

	public MasterContacter(InetAddress masterAddress, int masterPort, MasterContacterListener listener) {
		this.listener = listener;
		this.masterAddress = masterAddress;
		this.masterPort = masterPort;
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
		InetSocketAddress addr = new InetSocketAddress(masterAddress, masterPort);
		try (Socket s = new Socket()) {
			s.setSoTimeout(2000);
			s.connect(addr);
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			out.writeObject(m);
			out.flush();
			Object res = in.readObject();
			if (res instanceof Message) {
				Message responseMessage = (Message) res;
				switch (responseMessage.getCode()) {
				case CONNECT_RESPONSE:
					listener.onConnectResponse((ConnectResponse) responseMessage);
					break;
				case PONG:
					// Successful ping to master
					break;
				default:
					System.err.printf("Master sent invalid message %s%n", responseMessage.getClass().getSimpleName());
					break;
				}
			}
		} catch (IOException e) {
			if (m.getCode() == ClusterProtocol.CONNECT_REQUEST) {
				System.err.println("Failed to contact master.");
			} else {
				listener.masterTimeout();
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private int getNextDelay() {
		return listener.getStatus() == NodeState.NOT_CONNECTED ? DELAY_FAST_MSEC : DELAY_LONG_MSEC;
	}

	@Override
	public void run() {
		while (!close) {
			try {
				contactMaster();
				int delay = getNextDelay();
				Thread.sleep(delay);
			} catch (InterruptedException e) {
			}
		}
		System.out.println("closed " + this.getClass().getSimpleName());
	}

	@Override
	public void serviceFailure(Exception e) {
		// TODO Auto-generated method stub

	}

}
