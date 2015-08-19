package org.lancoder.common.network.cluster;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.lancoder.common.RunnableServiceAdapter;
import org.lancoder.common.pool.Pool;

public abstract class Server extends RunnableServiceAdapter {

	protected final static int MAX_HANDLERS = 10;

	protected int port;
	protected ServerSocket server;
	protected Pool<Socket> pool;
	protected Thread poolThread;

	public Server(int port) {
		this.port = port;
	}

	protected abstract void instanciatePool();

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
		instanciatePool();
		this.poolThread = new Thread(pool, pool.getClass().getSimpleName());
		this.poolThread.start();

		try {
			server = new ServerSocket(port);
			while (!close) {
				Socket incoming = server.accept();
				this.pool.add(incoming);
			}
		} catch (IOException e) {
			if (!close) {
				e.printStackTrace();
			}
		}
	}
}
