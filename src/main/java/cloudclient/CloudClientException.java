/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Images
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.cloudclient;

import javax.ws.rs.core.Response;

import javax.json.Json;

/**
 *
 * @author elz24996
 */
public class CloudClientException extends Exception implements ResponseProducer {
    
    private String message;
    protected int status;
    
    public CloudClientException(String message){
        this.status = 400;
        this.message = message;
    }
    
    public String getMessage(){
        return this.message;
    }
    
    public String toString(){
        return Json.createObjectBuilder().add("message", (String) getMessage()).build().toString();
    }
    
    public Response toResponse(){
        return Response.status(status).entity(toString()).build();
    }
    
}
