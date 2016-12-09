/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.rest;

import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.io.*;

import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.ejb.EJB;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.json.Json;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcatdaaasplugin.cloudclient.CloudClient;
import org.icatproject.topcatdaaasplugin.exceptions.DaaasException;

import org.icatproject.topcatdaaasplugin.database.Database;
import org.icatproject.topcatdaaasplugin.database.entities.*;

/**
 *
 * @author elz24996
 */
@Stateless
@LocalBean
@Path("admin")
public class AdminResource {
    
    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);

    @EJB
    CloudClient cloudClient;

    @EJB
    Database database;

    @GET
    @Path("/flavors")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFlavors() {
        try {
            return cloudClient.getFlavors().toResponse();
        } catch(DaaasException e) {
            return e.toResponse();
        }
    }

    @GET
    @Path("/images")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getImages() {
        try {
            return cloudClient.getImages().toResponse();
        } catch(DaaasException e) {
            return e.toResponse();
        }
    }


    @GET
    @Path("/availabilityZones")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAvailabilityZones() {
        try {
            return cloudClient.getAvailabilityZones().toResponse();
        } catch(DaaasException e) {
            return e.toResponse();
        }
    }

    @GET
    @Path("/machineTypes")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getMachineTypes() {
        try {
            return database.query("select machineType from MachineType machineType").toResponse();
        } catch(DaaasException e) {
            return e.toResponse();
        }
    }

    @POST
    @Path("/machineTypes")
    @Produces({MediaType.APPLICATION_JSON})
    public Response createMachineType(@FormParam("json") String json) {

        try {

            InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            JsonReader jsonReader = Json.createReader(jsonInputStream);
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            MachineType machineType = new MachineType();

            machineType.setName(jsonObject.getString("name"));
            machineType.setImageId(jsonObject.getString("imageId"));
            machineType.setFlavorId(jsonObject.getString("flavorId"));
            machineType.setAvailabilityZone(jsonObject.getString("availabilityZone"));
            machineType.setPoolSize(jsonObject.getInt("poolSize"));
            machineType.setPersonality(jsonObject.getString("personality"));

            List<MachineTypeScope> machineTypeScopes = new ArrayList<MachineTypeScope>();
            for(JsonValue machineTypeScopeValue : jsonObject.getJsonArray("scopes")){
                MachineTypeScope machineTypeScope = new MachineTypeScope();
                machineTypeScope.setMachineType(machineType);
                machineTypeScope.setQuery(((JsonObject) machineTypeScopeValue).getString("query"));
                machineTypeScopes.add(machineTypeScope);
            }
            machineType.setMachineTypeScopes(machineTypeScopes);

            database.persist(machineType);

            return machineType.toResponse();
        } catch(Exception e){
            return Response.status(400).entity(Json.createObjectBuilder().add("message", e.toString()).build().toString()).build();
        }
    }

    @PUT
    @Path("/machineTypes/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateMachineType(
        @PathParam("id") Integer id,
        @FormParam("json") String json) {

        try {

            MachineType machineType = (MachineType) database.query("select machineType from MachineType machineType where machineType.id = " + id).get(0);

            InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            JsonReader jsonReader = Json.createReader(jsonInputStream);
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            machineType.setName(jsonObject.getString("name"));
            machineType.setImageId(jsonObject.getString("imageId"));
            machineType.setFlavorId(jsonObject.getString("flavorId"));
            machineType.setAvailabilityZone(jsonObject.getString("availabilityZone"));
            machineType.setPoolSize(jsonObject.getInt("poolSize"));
            machineType.setPersonality(jsonObject.getString("personality"));
            
            List<MachineTypeScope> machineTypeScopes = new ArrayList<MachineTypeScope>();
            for(JsonValue machineTypeScopeValue : jsonObject.getJsonArray("scopes")){
                MachineTypeScope machineTypeScope = new MachineTypeScope();
                machineTypeScope.setMachineType(machineType);
                machineTypeScope.setQuery(((JsonObject) machineTypeScopeValue).getString("query"));
                machineTypeScopes.add(machineTypeScope);
            }
            machineType.setMachineTypeScopes(machineTypeScopes);

            database.persist(machineType);

            return machineType.toResponse();
        } catch(Exception e){
            return Response.status(400).entity(Json.createObjectBuilder().add("message", e.toString()).build().toString()).build();
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
            cloudClient.deleteMachine(machineId);
            return Response.ok().build();
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
