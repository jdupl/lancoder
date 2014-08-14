package drfoliberg.master.api.web;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import drfoliberg.common.network.messages.api.ApiJobRequest;
import drfoliberg.common.network.messages.api.ApiResponse;
import drfoliberg.master.Master;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.Gson;

public class ApiHandler extends AbstractHandler {

	private Master master;

	public ApiHandler(Master master) {
		this.master = master;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		Gson gson = new Gson();
		ApiResponse res = new ApiResponse(false, "Unknown error");
		BufferedReader br = null;
		response.setContentType("text/json;charset=utf-8");

		switch (target) {
		case "/nodes":
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().println(gson.toJson(master.getNodes()));
			break;
		case "/jobs":
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().println(gson.toJson(master.getJobs()));
			break;
		case "/jobs/add":
			br = request.getReader();
			try {
				ApiJobRequest req = gson.fromJson(br, ApiJobRequest.class);
				master.addJob(req);
				res = new ApiResponse(true);
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}

			response.getWriter().println(gson.toJson(res));
			baseRequest.setHandled(true);
			break;
		case "/jobs/delete":
			br = request.getReader();
			try {
				String id = br.readLine();
				response.setStatus(HttpServletResponse.SC_OK);
				res = master.apiDeleteJob(id);
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
			response.getWriter().println(gson.toJson(res));
			baseRequest.setHandled(true);
			break;
		default:
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}
}
