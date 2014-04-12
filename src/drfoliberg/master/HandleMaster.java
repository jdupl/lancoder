package drfoliberg.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import drfoliberg.common.Node;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.Message;
import drfoliberg.common.network.UNID;
import drfoliberg.common.network.StatusReport;
import drfoliberg.common.network.TaskReport;

public class HandleMaster implements Runnable {

	ObjectInputStream in;
	ObjectOutputStream out;
	Socket s;
	Master master;

	public HandleMaster(Socket s, Master master) {
		this.master = master;
		this.s = s;
	}

	public void run() {
		boolean close = false;
		try {
			out = new ObjectOutputStream(s.getOutputStream());
			out.flush();
			in = new ObjectInputStream(s.getInputStream());
			Node sender = null;
			while (!close) {
				Object request = in.readObject();
				if (request instanceof Message) {
					switch (((Message) request).getCode()) {

					case CONNECT_ME:
						sender = ((Message) request).getNode();
						boolean added = master.addNode(sender);
						if (added) {
							// send UNID to the node
							System.out.println("MASTER HANDLE: Sending unid to node");
							out.writeObject(new UNID(sender.getUnid()));
							out.flush();
						} else {
							out.writeObject(new Message(ClusterProtocol.BYE));
							out.flush();
							close = true;
							s.close();
						}
						break;
					case DISCONNECT_ME:
						sender = ((Message) request).getNode();
						//this.master.disconnectNode(sender);
						this.master.nodeShutdown(sender);
						out.writeObject(new Message(ClusterProtocol.BYE));
						out.flush();
						close = true;
						s.close();
						break;

					case TASK_REPORT:
						if (request instanceof TaskReport) {
							System.out.println("MASTER HANDLE: received a task report from worker.");
							TaskReport report = (TaskReport) request;
							this.master.readTaskReport(report);
						} else {
							System.out.println("MASTER HANDLE: received an INVALID task report from worker !");
						}
						out.writeObject(new Message(ClusterProtocol.BYE));
						close = true;
						s.close();
						break;
					case STATUS_REPORT:
						if (request instanceof StatusReport) {
							System.out.println("MASTER HANDLE: node updates it's status!");
							StatusReport report = (StatusReport) request;
							master.readStatusReport(report);
						}
					case BYE:
						close = true;
						s.close();
						break;
					default:
						out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
						s.close();
						close = true;
						break;
					}
				} else {
					out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
					s.close();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
