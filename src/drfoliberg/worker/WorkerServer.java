package drfoliberg.worker;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import drfoliberg.common.Status;
import drfoliberg.common.network.ClusterProtocol;
import drfoliberg.common.network.Message;
import drfoliberg.common.network.StatusReport;
import drfoliberg.common.network.TaskReport;
import drfoliberg.common.network.TaskRequestMessage;

public class WorkerServer implements Runnable {

	private Worker worker;
	private boolean close;

	public WorkerServer(Worker w) {
		this.worker = w;
		close = false;
	}

	public void shutdown() {
		this.close = true;
	}

	public void print(String s) {
		System.out.println(worker.getWorkerName().toUpperCase() + " SERVER: " + s);
	}

	public void run() {
		try {
			ServerSocket server = new ServerSocket(worker.getListenPort());
			server.setSoTimeout(2000);
			while (!close) {
				try {
					Socket s = server.accept();

					ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
					out.flush();
					ObjectInputStream in = new ObjectInputStream(s.getInputStream());

					Object o = in.readObject();

					if (o instanceof Message) {
						Message m = (Message) o;

						switch (m.getCode()) {

						case DISCONNECT_ME:
							print("master wants me to disconnect !");
							out.writeObject(new Message(ClusterProtocol.BYE));
							out.flush();
							s.close();
							worker.updateStatus(Status.NOT_CONNECTED);
							break;
						case TASK_REQUEST:
							print("received task from master");
							if(!(m instanceof TaskRequestMessage)) {
								out.writeObject(new Message(ClusterProtocol.TASK_REFUSED));
								out.flush();
								break;
							}
							TaskRequestMessage tqm = (TaskRequestMessage) m;

							if (worker.startWork(tqm.task)) {
								out.writeObject(new Message(ClusterProtocol.TASK_ACCEPTED));
							} else {
								out.writeObject(new Message(ClusterProtocol.TASK_REFUSED));
							}
							out.flush();
							break;
						case STATUS_REQUEST:
                            // if worker has no task, add null task report
							StatusReport report = null;
                            TaskReport taskReport = null;
							if (this.worker.getCurrentTask() != null) {
								taskReport = new TaskReport(worker.config.getUniqueID());
								taskReport.setProgress(this.worker.getCurrentTask().getProgress());
                                taskReport.setJobId(worker.getCurrentTask().getJobId());
                                taskReport.setTaskId(worker.getCurrentTask().getTaskId());
							}
                            report = new StatusReport(worker.getStatus(),worker.config.getUniqueID(),taskReport);
                            out.writeObject(report);
							out.flush();
						case BYE:
							s.close();
							break;
						default:
							out.writeObject(new Message(ClusterProtocol.BAD_REQUEST));
							break;
						}
					} else {
						print("could not read packet !");
					}
				} catch (InterruptedIOException e) {
					// interrupting every 2 seconds
				}
			}
			// closing socket
			System.out.println("Closing worker server !");
			if (server != null) {
				server.close();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
