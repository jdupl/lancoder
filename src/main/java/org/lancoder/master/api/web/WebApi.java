package org.lancoder.master.api.web;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lancoder.common.annotations.NoWebUI;
import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.codecs.CodecTypeAdapter;
import org.lancoder.common.network.messages.web.ApiJobRequest;
import org.lancoder.common.network.messages.web.ApiResponse;
import org.lancoder.master.impl.Master;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class WebApi {

	private Master master;
	private WebApiListener eventListener;
	private static Gson gson;

	public WebApi(Master master, WebApiListener eventListener) {
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
	public Response getNodes() {
		return Response.status(200).entity(gson.toJson(master.getNodeManager().getNodes())).build();
	}

	@POST
	@Path("/nodes/shutdown")
	public Response shutdownNode(String unid) {
        if (unid == null || unid.equals("")) {
            return Response.status(400).build();
        }

        eventListener.disconnectNode(unid);
        return Response.status(202).build();
	}

    @GET
    @Path("/jobs")
    public Response getJobs() {
        return Response.status(200).entity(gson.toJson(master.getJobManager().getJobs())).build();
    }

    @POST
    @Path("/jobs")
    public Response addJob(String req) {
        ApiJobRequest jobRequest = gson.fromJson(req, ApiJobRequest.class);

        if (eventListener.addJob(jobRequest)) {
            return Response.status(200).build();
        }

        ApiResponse res = new ApiResponse(false, "The file or directory does not exist.");
        return Response.status(400).entity(gson.toJson(res)).build();
    }

    @DELETE
    @Path("/jobs/{id}")
    public Response deleteJob(@PathParam("id") String jobId) {
        if (jobId == null || jobId.equals("")){
            return Response.status(400).build();
        }
        eventListener.apiDeleteJob(jobId);
        return Response.status(202).build();
    }

    @GET
    @Path("/jobs/clean")
    public Response cleanJobs() {
        master.cleanJobs();
        return Response.status(200).build();
    }

    @GET
    @Path("/codecs/audio")
    public Response getAudioCodecs() {
        return Response.status(200).entity(gson.toJson(CodecEnum.getAudioCodecs())).build();
    }

    @GET
    @Path("/codecs/video")
    public Response getVideoCodecs() {
        return Response.status(200).entity(gson.toJson(CodecEnum.getVideoCodecs())).build();
    }

}
