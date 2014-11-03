package org.lancoder.master.api.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.lancoder.common.RunnableService;
import org.lancoder.common.events.EventListener;
import org.lancoder.master.NodeManager;

public class MasterServer extends RunnableService {

	private int port;
	private MasterHandlePool pool;

	public MasterServer(EventListener listener, int port, NodeManager nodeManager) {
		this.port = port;
		this.pool = new MasterHandlePool(nodeManager, listener);
	}

	@Override
	public void run() {
		ServerSocket server;
		try {
			server = new ServerSocket(port);
			while (!close) {
				Socket incoming = server.accept();
				pool.handle(incoming);
			}
		} catch (IOException e) {
			if (!close) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		// TODO Auto-generated method stub
		e.printStackTrace();
	}

}
