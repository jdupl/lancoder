package org.lancoder.master.api.web;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.lancoder.common.annotations.NoWebUI;
import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.codecs.CodecTypeAdapter;
import org.lancoder.common.network.messages.web.ApiJobRequest;
import org.lancoder.common.network.messages.web.ApiResponse;
import org.lancoder.master.Master;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
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
		Gson gson = new GsonBuilder().registerTypeAdapter(CodecEnum.class, new CodecTypeAdapter<>())
				.setExclusionStrategies(new ExclusionStrategy() {
					@Override
					public boolean shouldSkipField(FieldAttributes f) {
						return f.getAnnotation(NoWebUI.class) != null;
					}

					@Override
					public boolean shouldSkipClass(Class<?> clazz) {
						return false;
					}
				}).serializeSpecialFloatingPointValues().create();
		ApiResponse res = new ApiResponse(false, "Unknown error");
		BufferedReader br = null;
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		switch (target) {
		case "/nodes":
			response.getWriter().println(gson.toJson(master.getNodeManager().getNodes()));
			break;
		case "/nodes/shutdown":
			br = request.getReader();
			String unid = br.readLine();
			if (unid != null) {
				master.disconnectNode(unid);
			}
			break;
		case "/jobs":
			response.getWriter().println(gson.toJson(master.getJobManager().getJobs()));
			break;
		case "/jobs/add":
			br = request.getReader();
			ApiJobRequest req = gson.fromJson(br, ApiJobRequest.class);
			if (master.addJob(req)) {
				res = new ApiResponse(true);
			} else {
				res = new ApiResponse(false, "The file or directory does not exist.");
			}
			response.getWriter().println(gson.toJson(res));
			break;
		case "/jobs/delete":
			br = request.getReader();
			String id = br.readLine();
			res = master.apiDeleteJob(id);
			response.getWriter().println(gson.toJson(res));
			break;
		case "/jobs/clean":
			master.cleanJobs();
			break;
		case "/codecs/audio":
			response.getWriter().println(gson.toJson(CodecEnum.getAudioCodecs()));
			break;
		case "/codecs/video":
			response.getWriter().println(gson.toJson(CodecEnum.getVideoCodecs()));
			break;
		default:
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}
}
