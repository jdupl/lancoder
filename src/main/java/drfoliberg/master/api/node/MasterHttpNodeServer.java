package drfoliberg.master.api.node;

import javax.servlet.http.HttpServlet;

import drfoliberg.common.ServerListener;
import drfoliberg.common.ServletServer;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class MasterHttpNodeServer extends ServletServer {

	public MasterHttpNodeServer(int port, MasterNodeServletListener servletListener, ServerListener serverListener) {
		super(port, servletListener, serverListener);

		HttpServlet servlet = new MasterNodeServlet(servletListener);
		ServletHolder holder = new ServletHolder();
		ServletContextHandler handler = new ServletContextHandler();

		holder.setServlet(servlet);
		handler.addServlet(holder, "/");
		server.setHandler(handler);
	}
}
