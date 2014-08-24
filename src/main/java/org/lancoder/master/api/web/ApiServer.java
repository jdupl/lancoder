package org.lancoder.master.api.web;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.lancoder.common.RunnableService;
import org.lancoder.master.Master;

public class ApiServer extends RunnableService {

	private static final String webDir = "web_resources/";

	private Master master;
	Server server;

	public ApiServer(Master master) {
		this.master = master;
	}

	@Override
	public void run() {
		server = new Server(master.getConfig().getApiServerPort());

		ContextHandler ctxStatic = new ContextHandler("/");
		ContextHandler ctxApi = new ContextHandler("/api");

		// static resources handler
		ResourceHandler staticHandler = new ResourceHandler();

		//staticHandler.setResourceBase(this.getClass().getClassLoader().getResource(webDir).toExternalForm());
		staticHandler.setResourceBase("src/main/web/web_resources"); // avoid repackaging jar and rerun application on
																		// web files change
		staticHandler.setDirectoriesListed(true);
		ctxStatic.setHandler(staticHandler);

		// api handler
		ApiHandler apiHandler = new ApiHandler(master);
		ctxApi.setHandler(apiHandler);

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { ctxStatic, ctxApi });

		server.setHandler(contexts);

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			// TODO alert master api server api crashed
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		super.stop();
		try {
			server.stop();
			System.out.println("MASTER: Closed api server");
		} catch (Exception e) {
			System.err.println("MASTER: Could not close api server !");
			e.printStackTrace();
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		master.serverFailure(e, this);
	}

}