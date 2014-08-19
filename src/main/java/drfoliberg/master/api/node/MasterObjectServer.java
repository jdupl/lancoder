package drfoliberg.master.api.node;

import java.io.IOException;
import java.net.ServerSocket;

import drfoliberg.common.RunnableService;

public class MasterObjectServer extends RunnableService {

	private MasterNodeServerListener listener;
	private int port;
	
	public MasterObjectServer(MasterNodeServerListener listener, int port) {
		this.port = port;
		this.listener = listener;
	}

	@Override
	public void run() {
		ServerSocket server;
		try {
			server = new ServerSocket(port);
			while(!close){
				MasterHandler handler = new MasterHandler(server.accept(), listener);
				Thread t = new Thread(handler);
				t.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		// TODO Auto-generated method stub
		
	}

}
