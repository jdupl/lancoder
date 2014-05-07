package drfoliberg.master;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import drfoliberg.common.Service;

public class MasterNodeServer extends Service {

	private Master master;

	public MasterNodeServer(Master master) {
		this.master = master;
	}

	public void run() {
		ServerSocket server = null;
		System.out.println("MASTER: Node server started");
		try {
			server = new ServerSocket(master.getConfig().getNodeServerPort());
			server.setSoTimeout(1000);
			while (!close) {
				try {
					Socket clientSocket = server.accept();
					MasterHandle handle = new MasterHandle(clientSocket, master);
					Thread t = new Thread(handle);
					t.start();
				} catch (InterruptedIOException e) {
					System.err.println("TIMEOUT");
					System.out.println(close);
					// thread was interrupted by master (master is shutting down)
				}
			}
			// closing thread as master is shutting down

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					server.close();
				} catch (IOException e) {
					System.err.println("Could not close server !");
				}
			}
		}
		System.out.println("MASTER: Node server closed");
	}
}
