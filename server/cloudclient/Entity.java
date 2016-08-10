/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.cloudclient;

import javax.ws.rs.core.Response;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author elz24996
 */
public abstract class Entity implements ResponseProducer {
    
    protected CloudClient cloudClient;
    
    public Entity(CloudClient cloudClient){
        this.cloudClient  = cloudClient;
    }
   
    public abstract JsonObjectBuilder toJsonObjectBuilder();
    
    @Override
    public String toString(){
        return toJsonObjectBuilder().build().toString();
    }
    
    @Override
    public Response toResponse(){
        return Response.ok().entity(toString()).build();
    }
    
}
