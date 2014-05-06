package drfoliberg.master.api;

import org.eclipse.jetty.server.Server;

import drfoliberg.master.Master;

public class ApiServer implements Runnable {

	private Master master;

	public ApiServer(Master master) {
		this.master = master;
	}

	@Override
	public void run() {
		Server apiServer = new Server(8080);
		ApiHandler handler = new ApiHandler(master);
		apiServer.setHandler(handler);
		try {
			apiServer.start();
			apiServer.join();
		} catch (Exception e) {
			// TODO alert master api server api crashed
			e.printStackTrace();
		}
	}

}
