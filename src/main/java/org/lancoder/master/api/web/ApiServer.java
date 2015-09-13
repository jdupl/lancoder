package org.lancoder.master.api.web;

import java.util.Properties;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.log.StdErrLog;
import org.lancoder.common.RunnableServiceAdapter;
import org.lancoder.master.impl.Master;

public class ApiServer extends RunnableServiceAdapter {

	private static final String WEB_DIR = "web_resources/";

	private Master master;
	Server server;

	public ApiServer(Master master) {
		this.master = master;
	}

	@Override
	public void run() {
		Properties jettyShutUpProperties = new Properties();
		jettyShutUpProperties.setProperty("org.eclipse.jetty.LEVEL", "WARN");
		StdErrLog.setProperties(jettyShutUpProperties);

		server = new Server(master.getConfig().getApiServerPort());

		ContextHandler ctxStatic = new ContextHandler("/");
		ContextHandler ctxApi = new ContextHandler("/api");

		// static resources handler
		ResourceHandler staticHandler = new ResourceHandler();

		staticHandler.setResourceBase(this.getClass().getClassLoader().getResource(WEB_DIR).toExternalForm());
//		staticHandler.setResourceBase("src/main/web/web_resources"); // avoid repackaging jar and rerun application on
																		// web files change
		staticHandler.setDirectoriesListed(true);
		ctxStatic.setHandler(staticHandler);

		// api handler
		ApiHandler apiHandler = new ApiHandler(master, master.getMasterEventCatcher());
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
		} catch (Exception e) {
			Logger logger = Logger.getLogger("lancoder");
			logger.severe(e.getMessage());
		}
	}
}