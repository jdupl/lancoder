package org.lancoder.master.api.web;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.lancoder.common.codecs.Codec;
import org.lancoder.common.codecs.CodecAdapterFactory;
import org.lancoder.common.network.messages.web.ApiJobRequest;
import org.lancoder.common.network.messages.web.ApiResponse;
import org.lancoder.master.Master;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
		response.setContentType("application/json");
		switch (target) {
		case "/nodes":
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().println(gson.toJson(master.getNodes()));
			break;
		case "/jobs":
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().println(master.getApiJobs());
			break;
		case "/jobs/add":
			br = request.getReader();
			try {
				ApiJobRequest req = gson.fromJson(br, ApiJobRequest.class);
				if (master.addJob(req)) {
					res = new ApiResponse(true);
				} else {
					res = new ApiResponse(false, "The file or directory does not exist.");
				}
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
		case "/codecs/audio":
			try {
				gson = new GsonBuilder().registerTypeAdapterFactory(new CodecAdapterFactory()).create();
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println(gson.toJson(Codec.getAudioCodecs()));
				baseRequest.setHandled(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}
}
