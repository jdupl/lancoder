package main.java.drfoliberg.worker.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;

import main.java.drfoliberg.common.network.Routes;
import main.java.drfoliberg.common.network.messages.cluster.StatusReport;
import main.java.drfoliberg.common.network.messages.cluster.TaskRequestMessage;

import com.google.gson.Gson;

public class WorkerServlet extends HttpServlet implements WorkerServletListerner {

	private static final long serialVersionUID = 6070828632381509848L;

	WorkerServletListerner servletListener;

	public WorkerServlet(WorkerServletListerner servletListener) {
		this.servletListener = servletListener;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		switch (req.getRequestURI()) {
		case Routes.PAUSE_TASK:
			resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
			break;
		case Routes.DISCONNECT_NODE:
			this.shutdownWorker();
			break;
		case Routes.PAUSE_NODE:
			resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Gson gson = new Gson();
		resp.setCharacterEncoding(StandardCharsets.UTF_8.toString());
		resp.setContentType(ContentType.APPLICATION_JSON.toString());
		switch (req.getRequestURI()) {
		case Routes.NODE_STATUS:
			StatusReport report = statusRequest();
			resp.getWriter().print(gson.toJson(report));
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Gson gson = new Gson();
		switch (req.getRequestURI()) {
		case Routes.ADD_TASK:
			TaskRequestMessage tqm = gson.fromJson(req.getReader(), TaskRequestMessage.class);
			if (tqm == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else if (!taskRequest(tqm)) {
				resp.sendError(HttpServletResponse.SC_CONFLICT);
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Gson gson = new Gson();
		switch (req.getRequestURI()) {
		case Routes.DELETE_TASK:
			TaskRequestMessage tqm = gson.fromJson(req.getReader(), TaskRequestMessage.class);
			if (tqm == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else if (!deleteTask(tqm)) {
				resp.sendError(HttpServletResponse.SC_NO_CONTENT);
			}
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}

	@Override
	public boolean taskRequest(TaskRequestMessage tqm) {
		return servletListener.taskRequest(tqm);
	}

	@Override
	public StatusReport statusRequest() {
		return this.servletListener.statusRequest();
	}

	@Override
	public boolean deleteTask(TaskRequestMessage tqm) {
		return servletListener.deleteTask(tqm);
	}

	@Override
	public void shutdownWorker() {
		servletListener.shutdownWorker();	
	}
}
