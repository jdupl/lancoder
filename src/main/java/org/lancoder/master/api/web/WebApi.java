package org.lancoder.master.api.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class WebApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMsg() {
        return Response.status(200).entity("{}").build();

    }

}
