package drfoliberg.master;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;

public class MasterServer extends Thread {

	private Master master;

	public MasterServer(Master master) {
		this.master = master;
	}

	public void run() {
		try {
			boolean close = false;
			ServerSocket server = new ServerSocket(1337);
			while (!close) {
				try {
					new HandleMaster(server.accept(), master).start();
				} catch (InterruptedIOException e) {
					// thread was interrupted by master (master is shutting down)
					close = true;
				}
			}
			// closing thread as master is shutting down
			System.out.println("MASTER: Closing master server !");
			if (server != null) {
				server.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
