/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.responseproducer;

import java.math.BigDecimal;
import java.util.ArrayList;
import javax.ws.rs.core.Response;
import javax.json.Json;
import javax.json.JsonArrayBuilder;

/**
 *
 * @author elz24996
 */
public class EntityList<T> extends ArrayList<T> implements ResponseProducer {
    
    @Override
    public String toString(){
        JsonArrayBuilder out = Json.createArrayBuilder();
        for(T entity : this){
            out.add(((Entity) entity).toJsonObjectBuilder());
        }
        return out.build().toString();
    }
    
    @Override
    public Response toResponse(){
        return Response.ok().entity(toString()).build();
    }
    
}
