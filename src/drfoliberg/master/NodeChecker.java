package drfoliberg.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import drfoliberg.common.Node;
import drfoliberg.common.Service;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.messages.cluster.Message;
import drfoliberg.common.network.messages.cluster.StatusReport;

public class NodeChecker extends Service {

	private final static int MS_DELAY_BETWEEN_CHECKS = 5000;
	Master master;

	public NodeChecker(Master master) {
		this.master = master;
	}

	private boolean checkNodes() {
		if (master.getNodes().size() == 0) {
			System.out.println("MASTER NODE CHECKER: no nodes to check!");
			return false;
		}
		System.out.println("MASTER NODE CHECKER: checking if nodes are still alive");
		for (Node n : master.getOnlineNodes()) {
			System.out.println("MASTER NODE CHECKER: checking node: " + n.getNodeAddress().toString());
			// update the node list and task list
			Socket s = null;
			try {
				s = new Socket(n.getNodeAddress(), n.getNodePort());
				// a timeout of 2 seconds
				s.setSoTimeout(2000);
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				out.flush();
				ObjectInputStream in = new ObjectInputStream(s.getInputStream());
				out.writeObject(new Message(ClusterProtocol.STATUS_REQUEST));
				out.flush();
				Object o = in.readObject();
				if (o instanceof Message) {
					Message m = (Message) o;
					switch (m.getCode()) {
					case STATUS_REPORT:
						// read status report and update node if necessary
						if (m instanceof StatusReport) {
							System.out.println("MASTER NODE CHECKER: node " + n.getName()
									+ " is still alive and sent valid status report");
							StatusReport statusReport = ((StatusReport) m);
							if (statusReport.getTaskReport() != null) {
								this.master.readTaskReport(statusReport.getTaskReport());
							}
							this.master.readStatusReport(statusReport);
						} else {
							System.err.println("Could not read valid StatusReport from node " + n.getName());
						}
						break;

					default:
						System.err.println("Unexpected message code " + m.getCode() + " from node " + n.getName());
						break;
					}
				}
				s.close();
			} catch (IOException e) {
				// this node failed !
				e.printStackTrace();
				System.out.println("NODE CHECKER: node " + n.getName() + " failed ! Advising master!");
				this.master.removeNode(n);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public void run() {
		System.out.println("Starting node checker service!");
		while (!close) {
			try {
				checkNodes();
				System.out.println("NODE CHECKER: checking back in 5 seconds");
				// TODO check for better way to sleep / handle interrupt
				Thread.currentThread();
				Thread.sleep(MS_DELAY_BETWEEN_CHECKS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Closed node checker service!");
	}
}