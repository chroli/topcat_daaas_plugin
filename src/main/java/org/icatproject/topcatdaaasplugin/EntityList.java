/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin;

import org.icatproject.topcatdaaasplugin.rest.ResponseProducer;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

/**
 * @author elz24996
 */
public class EntityList<T> extends ArrayList<T> implements ResponseProducer {

    @Override
    public String toString() {
        return toJsonArrayBuilder().build().toString();
    }

    public JsonArrayBuilder toJsonArrayBuilder() {
        JsonArrayBuilder out = Json.createArrayBuilder();
        for (T entity : this) {
            out.add(((Entity) entity).toJsonObjectBuilder());
        }
        return out;
    }

    @Override
    public Response toResponse() {
        return Response.ok().entity(toString()).build();
    }

}
