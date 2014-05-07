package drfoliberg.master.api;

import org.eclipse.jetty.server.Server;

import drfoliberg.common.Service;
import drfoliberg.master.Master;

public class ApiServer extends Service {

	private Master master;
	Server apiServer;
	ApiHandler handler;

	public ApiServer(Master master) {
		this.master = master;
	}

	@Override
	public void run() {
		apiServer = new Server(8080);
		handler = new ApiHandler(master);
		apiServer.setHandler(handler);
		try {
			apiServer.start();
			apiServer.join();
		} catch (Exception e) {
			// TODO alert master api server api crashed
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() {
		super.stop();
		try {
			apiServer.stop();
			System.out.println("MASTER: Closed api server");
		} catch (Exception e) {
			System.err.println("MASTER: Could not close api server !");
			e.printStackTrace();
		}
	}

}
