package org.lancoder.master.api.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lancoder.common.annotations.NoWebUI;
import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.codecs.CodecTypeAdapter;
import org.lancoder.master.impl.Master;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path("/")
public class WebApi {

	private Master master;
	private ApiHandlerListener eventListener;
	private static Gson gson;

	public WebApi(Master master, ApiHandlerListener eventListener) {
		this.master = master;
		this.eventListener = eventListener;
		WebApi.gson = new GsonBuilder().registerTypeAdapter(CodecEnum.class, new CodecTypeAdapter<>())
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
	}

	@GET
	@Path("/nodes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNodes() {
		return Response.status(200).entity(gson.toJson(master.getNodeManager().getNodes())).build();
	}

}
