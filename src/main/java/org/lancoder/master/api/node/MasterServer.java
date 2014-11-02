package org.lancoder.master.api.node;

import java.io.IOException;
import java.net.ServerSocket;

import org.lancoder.common.RunnableService;
import org.lancoder.common.events.EventListener;
import org.lancoder.master.NodeManager;

public class MasterServer extends RunnableService {

	private EventListener listener;
	private int port;
	private NodeManager nodeManager;

	public MasterServer(EventListener listener, int port, NodeManager nodeManager) {
		this.port = port;
		this.listener = listener;
		this.nodeManager = nodeManager;
	}

	@Override
	public void run() {
		ServerSocket server;
		try {
			server = new ServerSocket(port);
			while (!close) {
				MasterHandler handler = new MasterHandler(server.accept(), listener, nodeManager);
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
