package org.lancoder.worker.server;

import java.io.IOException;
import java.net.ServerSocket;

import org.lancoder.common.RunnableService;

public class WorkerObjectServer extends RunnableService {

	private int port;
	private HandlerPool pool;

	public WorkerObjectServer(WorkerServerListener listener, int port) {
		this.port = port;
		this.pool = new HandlerPool(100, listener);
	}

	@Override
	public void run() {
		ServerSocket server;
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
