package main.java.drfoliberg.master.api.node;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import main.java.drfoliberg.common.network.Routes;
import main.java.drfoliberg.common.network.messages.cluster.ConnectMessage;
import main.java.drfoliberg.common.network.messages.cluster.StatusReport;

import org.apache.http.entity.ContentType;

import com.google.gson.Gson;

public class MasterNodeServlet extends HttpServlet {

	private static final long serialVersionUID = 8887369974516325892L;

	MasterNodeServletListener listener;

	public MasterNodeServlet(MasterNodeServletListener listener) {
		this.listener = listener;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Gson gson = new Gson();
		switch (req.getRequestURI()) {
		case Routes.CONNECT_NODE:
			ConnectMessage cm = gson.fromJson(req.getReader(), ConnectMessage.class);
			if (cm == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				this.listener.readConnectRequest(cm);
				resp.setCharacterEncoding(StandardCharsets.UTF_8.toString());
				resp.setContentType(ContentType.TEXT_PLAIN.toString());
				resp.getWriter().print(cm.getUnid());
			}
			break;
		case Routes.NODE_STATUS:
			StatusReport report = gson.fromJson(req.getReader(), StatusReport.class);
			listener.readStatusReport(report);
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}

}
