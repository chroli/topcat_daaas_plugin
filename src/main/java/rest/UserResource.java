/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.rest;

import java.util.Map;
import java.util.HashMap;

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

import javax.json.JsonObject;
import javax.json.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcatdaaasplugin.cloudclient.CloudClient;
import org.icatproject.topcatdaaasplugin.database.Database;
import org.icatproject.topcatdaaasplugin.database.entities.*;
import org.icatproject.topcatdaaasplugin.exceptions.DaaasException;
import org.icatproject.topcatdaaasplugin.*;

/**
 *
 * @author elz24996
 */
@Stateless
@LocalBean
@Path("user")
public class UserResource {
    
    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);

    @EJB
    CloudClient cloudClient;

    @EJB
    Database database;

    @EJB
    MachinePool machinePool;

    @GET
    @Path("/machines")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getMachines(
        @QueryParam("icatUrl") String icatUrl,
        @QueryParam("sessionId") String sessionId){
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("owner", getUsername(icatUrl, sessionId));
            return database.query("select machine from Machine machine where machine.owner = :owner", params).toResponse();
        } catch(DaaasException e) {
            return e.toResponse();
        } catch(Exception e){
            return new DaaasException(e.getMessage()).toResponse();
        }
    }

    @POST
    @Path("/machines")
    @Produces({MediaType.APPLICATION_JSON})
    public Response createMachine(
        @QueryParam("icatUrl") String icatUrl,
        @QueryParam("sessionId") String sessionId,
        @QueryParam("machineTypeId") Long machineTypeId){
        try {
            if(!isMachineTypeAllowed(icatUrl, sessionId, machineTypeId)){
                 throw new DaaasException("You are not allowed to create this machine type.");
            }
            Machine machine = machinePool.aquireMachine(machineTypeId);
            machine.setOwner(getUsername(icatUrl, sessionId));
            database.persist(machine);
            return machine.toResponse();
        } catch(DaaasException e) {
            return e.toResponse();
        } catch(Exception e){
            return new DaaasException(e.getMessage()).toResponse();
        }
    }

    @GET
    @Path("/machineTypes")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getMachineTypes(
        @QueryParam("icatUrl") String icatUrl,
        @QueryParam("sessionId") String sessionId){
        try {
            return getAvailableMachineTypes(icatUrl, sessionId).toResponse();
        } catch(DaaasException e) {
            return e.toResponse();
        } catch(Exception e){
            return new DaaasException(e.getMessage()).toResponse();
        }
    }

    private boolean isMachineTypeAllowed(String icatUrl, String sessionId, Long machineTypeId) throws Exception {
        for(MachineType machineType : getAvailableMachineTypes(icatUrl, sessionId)){
            if(machineType.getId().equals(machineTypeId)){
                return true;
            }
        }
        return false;
    }

    private EntityList<MachineType> getAvailableMachineTypes(String icatUrl, String sessionId) throws Exception {
        EntityList<MachineType> out = new EntityList<MachineType>();
        IcatClient icatClient = new IcatClient(icatUrl, sessionId);
        for(Entity machineTypeEntity : database.query("select machineType from MachineType machineType")){
            MachineType machineType = (MachineType) machineTypeEntity;
            for(MachineTypeScope machineTypeScope : machineType.getMachineTypeScopes()){
                if(icatClient.query(machineTypeScope.getQuery()).size() > 0){
                    out.add(machineType);
                    break;
                }
            }
        }
        return out;
    }

    private String getUsername(String icatUrl, String sessionId) throws Exception {
        IcatClient icatClient = new IcatClient(icatUrl, sessionId);
        JsonObject user = (JsonObject) icatClient.query("select user from User user where user.name = :user").get(0);
        return user.getString("name");
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
    //     } catch(DaaasException e) {
    //         return e.toResponse();
    //     }
    
    /*
    @POST
    @Path("/machines")
    @Produces({MediaType.APPLICATION_JSON})
    public Response createMachine(
            @FormParam("templateId") String templateId,
            @FormParam("name") String name) {
        
        try {
            return cloudClient.createMachine(templateId, name).toResponse();
        } catch(DaaasException e) {
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
        } catch(DaaasException e) {
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
        } catch(DaaasException e) {
            return e.toResponse();
        }
        
    }

    @GET
    @Path("/machineTypes")
    @Produces({MediaType.APPLICATION_JSON})
    public Response machineTypes(
            @QueryParam("sessionId") String sessionId) {
        
        try {
            
            
            return cloudClient.getTemplates().toResponse();
        } catch(DaaasException e) {
            return e.toResponse();
        }
        
    }

    @GET
    @Path("/test")
    @Produces({MediaType.TEXT_PLAIN})
    public Response test(){
        try {
            return Response.ok().entity(new SshClient("glassfish", "topcat-dev.esc.rl.ac.uk", "/home/vagrant/id_rsa").exec("ping -c 1 google.com")).build();
        } catch(Exception e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    // private String getUsername(String sessionId){
        
    // }

    */
}
