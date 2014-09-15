package org.lancoder.worker.contacter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.lancoder.common.RunnableService;
import org.lancoder.common.network.messages.ClusterProtocol;
import org.lancoder.common.network.messages.cluster.Message;
import org.lancoder.common.network.messages.cluster.PingMessage;
import org.lancoder.common.status.NodeState;

public class ContactMasterObject extends RunnableService {

	private final static int DELAY_FAST_MSEC = 5000;
	private final static int DELAY_LONG_MSEC = 30000;

	private ConctactMasterListener listener;
	private InetAddress masterAddress;
	private int masterPort;

	public ContactMasterObject(InetAddress masterAddress, int masterPort, ConctactMasterListener listener) {
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
			if (res instanceof String) {
				String unid = (String) res;
				if (unid != null && !unid.isEmpty()) {
					// this will trigger a node status change that will then stop this service
					listener.receivedUnid(unid);
				} else {
					System.err.println("Received null string or invalid string from master ?");
				}
			} else if (!(res instanceof Message) && ((Message) res).getCode() != ClusterProtocol.PONG) {
				System.err.println("Worker detected master fault when pinging !");
				// TODO alert somewhere
			}
		} catch (IOException e) {
			if (m.getCode() == ClusterProtocol.CONNECT_ME) {
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
	}

	@Override
	public void serviceFailure(Exception e) {
		// TODO Auto-generated method stub

	}

}
