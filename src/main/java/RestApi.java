/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin;

import javax.ejb.Stateless;
import javax.ejb.LocalBean;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcatdaaasplugin.cloudclient.CloudClient;
import org.icatproject.topcatdaaasplugin.cloudclient.CloudClientException;

/**
 *
 * @author elz24996
 */
@Stateless
@LocalBean
@Path("")
public class RestApi {
    
    private static final Logger logger = LoggerFactory.getLogger(RestApi.class);

    @EJB
    CloudClient cloudClient;

    @GET
    @Path("/machines")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getMachines() {
        try {
            return cloudClient.getMachines().toResponse();
        } catch(CloudClientException e) {
            return e.toResponse();
        }
        
    }
    
    // @GET
    // @Path("/user")
    // @Produces({MediaType.APPLICATION_JSON})
    // public Response getUser(
    //         @QueryParam("sessionId") String sessionId,
    //         @QueryParam("userId") String userId) {
        
    //     try {
    //         if(userId != null){
    //             return new CloudClient(sessionId).getUser(Integer.parseInt(userId)).toResponse();
    //         } else {
    //             return new CloudClient(sessionId).getUser().toResponse();
    //         }
    //     } catch(CloudClientException e) {
    //         return e.toResponse();
    //     }
    
    
    @POST
    @Path("/machines")
    @Produces({MediaType.APPLICATION_JSON})
    public Response createMachine(
            @FormParam("templateId") String templateId,
            @FormParam("name") String name) {
        
        try {
            return cloudClient.createMachine(templateId, name).toResponse();
        } catch(CloudClientException e) {
            return e.toResponse();
        }
        
    }
    
    @DELETE
    @Path("/machines/{machineId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteMachine(
            @PathParam("machineId") String machineId) {
        try {
            return cloudClient.deleteMachine(machineId).toResponse();
        } catch(CloudClientException e) {
            return e.toResponse();
        }
    }
    
    
    @GET
    @Path("/templates")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTemplates(
            @QueryParam("sessionId") String sessionId) {
        
        try {
            return cloudClient.getTemplates().toResponse();
        } catch(CloudClientException e) {
            return e.toResponse();
        }
        
    }
}