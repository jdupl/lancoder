package org.lancoder.worker.server;

import java.io.IOException;
import java.net.ServerSocket;

import org.lancoder.common.RunnableService;

public class WorkerServer extends RunnableService {

	private int port;
	private HandlerPool pool;
	private Thread poolThread;
	private ServerSocket server;

	public WorkerServer(WorkerServerListener listener, int port) {
		this.port = port;
		this.pool = new HandlerPool(100, listener);
	}

	@Override
	public void stop() {
		super.stop();
		this.pool.stop();
		poolThread.interrupt();
		try {
			server.close();
		} catch (IOException e) {
		}
	}

	@Override
	public void run() {
		this.poolThread = new Thread(pool);
		this.poolThread.start();
		try {
			server = new ServerSocket(port);
			while (!close) {
				this.pool.handle(server.accept());
			}
		} catch (IOException e) {
			if (!close) {
				serviceFailure(e);
			}
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}
}
