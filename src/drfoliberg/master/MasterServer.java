package drfoliberg.master;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MasterServer implements Runnable {

	private Master master;

	public MasterServer(Master master) {
		this.master = master;
	}

	public void run() {
		try {
			boolean close = false;
			ServerSocket server = new ServerSocket(1337);
			// TODO set timeout to interrupt listening on socket from time to time
			while (!close) {
				try {
					Socket clientSocket = server.accept();
					HandleMaster handle = new HandleMaster(clientSocket, master);
					
					Thread t = new Thread(handle);
					t.start();
				} catch (InterruptedIOException e) {
					// thread was interrupted by master (master is shutting down)
					//TODO add condition where interruption occurred by timeout (to check closed state)
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
