package drfoliberg.master.api;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.Gson;

import drfoliberg.common.job.Job;
import drfoliberg.common.job.JobType;
import drfoliberg.common.network.messages.api.ApiJobRequest;
import drfoliberg.master.Master;

public class ApiHandler extends AbstractHandler {

	private Master master;

	public ApiHandler(Master master) {
		this.master = master;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		System.err.println(target);
		Gson gson = new Gson();
		switch (target) {
		case "/nodes":
			response.setContentType("text/json;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().println(gson.toJson(master.getNodes()));
			break;
		case "/jobs":
			response.setContentType("text/json;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().println(gson.toJson(master.getJobs()));
			break;
		case "/jobs/add":
			BufferedReader br = request.getReader();
			ApiJobRequest req = gson.fromJson(br, ApiJobRequest.class);
			Job j = new Job(req.getName(), req.getInputFile(), master.getConfig().getAbsoluteSharedFolder(),
					JobType.BITRATE_2_PASS_JOB, 1 * 1000 * 60, req.getBitrate());
			master.addJob(j);
			response.setContentType("text/json;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			break;
		default:
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}
}
