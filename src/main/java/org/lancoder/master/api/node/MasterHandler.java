package org.lancoder.master.api.node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.lancoder.common.events.Event;
import org.lancoder.common.events.EventListener;
import org.lancoder.common.network.cluster.messages.ConnectRequest;
import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.messages.PingMessage;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.pool.PoolWorker;
import org.lancoder.master.NodeManager;

public class MasterHandler extends PoolWorker<Socket> {

	private EventListener listener;
	private NodeManager nodeManager;

	public MasterHandler(EventListener listener, NodeManager nodeManager) {
		this.listener = listener;
		this.nodeManager = nodeManager;
	}

	@Override
	protected void start() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(task.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(task.getInputStream());
			out.flush();
			Object request = in.readObject();
			if (request instanceof Message) {
				Message requestMessage = (Message) request;
				switch (requestMessage.getCode()) {
				case CONNECT_REQUEST:
					if (requestMessage instanceof ConnectRequest) {
						out.writeObject(nodeManager.connectRequest((ConnectRequest) requestMessage,
								task.getInetAddress()));
					} else {
						out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
					}
					break;
				case STATUS_REPORT:
					if (requestMessage instanceof StatusReport) {
						listener.handle(new Event((StatusReport) requestMessage));
						out.writeObject(new Message(ClusterProtocol.BYE));
					} else {
						out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
					}
					break;
				case DISCONNECT_ME:
					if (requestMessage instanceof ConnectRequest) {
						nodeManager.disconnectRequest((ConnectRequest) requestMessage);
						out.writeObject(new Message(ClusterProtocol.BYE));
					} else {
						out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
					}
					break;
				case PING:
					out.writeObject(PingMessage.getPong());
					break;
				default:
					out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
					break;
				}
			} else {
				out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
			}
			out.flush();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (task != null && !task.isClosed()) {
				try {
					task.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		// TODO Auto-generated method stub

	}
}
