package org.lancoder.worker.server;

import java.io.IOException;
import java.net.ServerSocket;

import org.lancoder.common.RunnableService;

public class WorkerObjectServer extends RunnableService {

	private WorkerServerListener listener;
	private int port;
	
	public WorkerObjectServer(WorkerServerListener listener, int port) {
		this.port = port;
		this.listener = listener;
	}

	@Override
	public void run() {
		ServerSocket server;
		try {
			server = new ServerSocket(port);
			while(!close){
				WorkerHandler handler = new WorkerHandler(server.accept(), listener);
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
