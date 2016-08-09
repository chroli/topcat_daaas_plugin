package org.icatproject.topcatdaaasplugin;

import javax.ejb.Stateless;
import javax.ejb.LocalBean;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;


@Stateless
@LocalBean
@Path("")
public class RestApi {
    /**
     * Used to detect whether Topcat is running or not.
     *
     * @summary ping
     *
     * @return a string "ok" if all is well
    */
    @GET
    @Path("/ping")
    @Produces({MediaType.APPLICATION_JSON})
    public Response ping() {
        return Response.ok().entity("\"ok\"").build();
    }

}
