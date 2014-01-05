package drfoliberg.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import drfoliberg.common.Node;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.Message;
import drfoliberg.common.network.StatusReport;

public class NodeChecker extends Thread {

	Master master;

	public NodeChecker(Master master) {
		this.master = master;
	}

	public void run() {
		System.out.println("Starting node checker service!");
		while (true) {
			try {
				if (master.getNodes().size() > 0) {
					System.out.println("MASTER NODE CHECKER: checking if nodes are still alive");
					for (Node n : master.getNodes()) {
						System.out.println("MASTER NODE CHECKER: checking node: " + n.getNodeAddress().toString());
						// update the node list and task list
						Socket s = null;
						try {
							s = new Socket(n.getNodeAddress(), n.getNodePort());
							// a timeout of 5 seconds
							s.setSoTimeout(5000);
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
									// if node is working, update the task status
									// TODO read report and update the progress
									if (m instanceof StatusReport) {
										System.out.println("MASTER NODE CHECKER: node " + n.getName()
												+ " is still alive and sent valid status report");
										StatusReport statusReport = ((StatusReport) m);
										this.master.readStatusReport(statusReport);
										this.master.readTaskReport(statusReport.getTaskReport());
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
							System.out.println("MASTER NODE CHECKER: node " + n.getName()
									+ " failed ! Advising master!");
							this.master.removeNode(n);
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					System.out.println("MASTER NODE CHECKER: no nodes to check!");
				}
				System.out.println("MASTER NODE CHECKER: checking back in 30 seconds");
				sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
