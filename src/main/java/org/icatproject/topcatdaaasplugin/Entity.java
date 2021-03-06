/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin;

import org.icatproject.topcatdaaasplugin.rest.ResponseProducer;

import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;

/**
 * @author elz24996
 */
public abstract class Entity implements ResponseProducer {

    public abstract JsonObjectBuilder toJsonObjectBuilder();

    @Override
    public String toString() {
        return toJsonObjectBuilder().build().toString();
    }

    @Override
    public Response toResponse() {
        return Response.ok().entity(toString()).build();
    }

}
