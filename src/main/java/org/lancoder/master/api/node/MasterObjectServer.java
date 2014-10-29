package org.lancoder.master.api.node;

import java.io.IOException;
import java.net.ServerSocket;

import org.lancoder.common.RunnableService;
import org.lancoder.master.Master;

public class MasterObjectServer extends RunnableService {

	private Master master;
	private int port;
	
	public MasterObjectServer(Master listener, int port) {
		this.port = port;
		this.master = listener;
	}

	@Override
	public void run() {
		ServerSocket server;
		try {
			server = new ServerSocket(port);
			while(!close){
				MasterHandler handler = new MasterHandler(server.accept(), master);
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
