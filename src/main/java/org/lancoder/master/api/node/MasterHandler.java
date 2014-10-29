package org.lancoder.master.api.node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.lancoder.common.events.Event;
import org.lancoder.common.network.cluster.messages.ConnectMessage;
import org.lancoder.common.network.cluster.messages.Message;
import org.lancoder.common.network.cluster.messages.PingMessage;
import org.lancoder.common.network.cluster.messages.StatusReport;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.master.Master;

public class MasterHandler implements Runnable {

	private Master master;
	private Socket s;

	public MasterHandler(Socket s, Master listener) {
		this.master = listener;
		this.s = s;
	}

	@Override
	public void run() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			out.flush();
			while (!s.isClosed()) {
				Object request = in.readObject();
				if (request instanceof Message) {
					Message requestMessage = (Message) request;
					switch (requestMessage.getCode()) {
					case CONNECT_ME:
						if (requestMessage instanceof ConnectMessage) {
							String unid = master.connectRequest((ConnectMessage) requestMessage, s.getInetAddress());
							out.writeObject(unid);
						} else {
							out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
						}
						out.flush();
						s.close();
						break;
					case STATUS_REPORT:
						if (requestMessage instanceof StatusReport) {
							master.handle(new Event((StatusReport) requestMessage));
							out.writeObject(new Message(ClusterProtocol.BYE));
						} else {
							out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
						}
						out.flush();
						s.close();
						break;
					case DISCONNECT_ME:
						if (requestMessage instanceof ConnectMessage) {
							master.disconnectRequest((ConnectMessage) requestMessage);
							out.writeObject(new Message(ClusterProtocol.BYE));
						} else {
							out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
						}
						out.flush();
						s.close();
						break;
					case PING:
						out.writeObject(PingMessage.getPong());
						out.flush();
						s.close();
						break;
					default:
						out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
						out.flush();
						s.close();
						break;
					}
				} else {
					out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
					out.flush();
					s.close();
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (s != null && !s.isClosed()) {
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
