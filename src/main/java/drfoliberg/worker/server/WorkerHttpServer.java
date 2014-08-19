package drfoliberg.worker.server;

import javax.servlet.http.HttpServlet;

import drfoliberg.common.ServerListener;
import drfoliberg.common.ServletServer;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

@Deprecated
public class WorkerHttpServer extends ServletServer {

	public WorkerHttpServer(int port, WorkerServletListener servletListener, ServerListener serverListener) {
		super(port, servletListener, serverListener);

		HttpServlet servlet = new WorkerServlet(servletListener);
		ServletHolder holder = new ServletHolder();
		ServletContextHandler handler = new ServletContextHandler();

		holder.setServlet(servlet);
		handler.addServlet(holder, "/");
		server.setHandler(handler);
	}
}
