package main.java.drfoliberg.common;

import java.util.ArrayList;

import org.eclipse.jetty.server.Server;

public class ServletServer extends Service implements ServerListener {

	protected Server server;
	protected ArrayList<ServerListener> listeners;

	public ServletServer(int port, ServletListener servletListener, ServerListener serverListener) {
		this.listeners = new ArrayList<>();
		this.listeners.add(serverListener);
		this.server = new Server(port);
	}

	@Override
	public void run() {
		System.err.println("Starting worker server");
		try {
			server.start();
			server.join();
			System.err.println("Started worker server");
		} catch (Exception e) {
			serverFailure(e, this);
		}
	}

	@Override
	public void stop() {
		super.stop();
		try {
			server.stop();
		} catch (Exception e) {
			serverFailure(e, this);
		}
		serverShutdown(this);
	}

	@Override
	public void serverShutdown(Service server) {
		for (ServerListener listener : listeners) {
			listener.serverShutdown(server);
		}
	}

	@Override
	public void serverFailure(Exception e, Service server) {
		for (ServerListener listener : listeners) {
			listener.serverFailure(e, server);
		}
	}
}
