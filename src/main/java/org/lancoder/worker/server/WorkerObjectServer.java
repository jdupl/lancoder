package org.lancoder.worker.server;

import java.io.IOException;
import java.net.ServerSocket;

import org.lancoder.common.RunnableService;

public class WorkerObjectServer extends RunnableService {

	private int port;
	private HandlerPool pool;
	private Thread poolThread;

	public WorkerObjectServer(WorkerServerListener listener, int port) {
		this.port = port;
		this.pool = new HandlerPool(100, listener);
	}

	@Override
	public void run() {
		ServerSocket server;
		this.poolThread= new Thread(pool);
		this.poolThread.start();
		try {
			server = new ServerSocket(port);
			while (!close) {
				this.pool.handle(server.accept());
			}
		} catch (IOException e) {
			serviceFailure(e);
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}
}
