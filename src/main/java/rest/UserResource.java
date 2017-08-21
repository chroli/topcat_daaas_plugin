/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.rest;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.List;
import java.util.Date;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.ejb.Stateless;
import javax.ejb.LocalBean;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
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
import javax.ws.rs.core.CacheControl;

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
            params.put("primaryUser", getUsername(icatUrl, sessionId));
            return database.query("select machine from MachineUser machineUser, machineUser.machine as machine where machineUser.userName = :primaryUser", params).toResponse();
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
        @FormParam("icatUrl") String icatUrl,
        @FormParam("sessionId") String sessionId,
        @FormParam("machineTypeId") Long machineTypeId){

        logger.info("createMachine: user with sessionId " + sessionId + " is creating a machine with machineTypeId " + machineTypeId);

        try {
            if(!isMachineTypeAllowed(icatUrl, sessionId, machineTypeId)){
                throw new DaaasException("You are not allowed to create this machine type.");
            }
            Machine machine = machinePool.aquireMachine(machineTypeId);
            if(machine == null){
                throw new DaaasException("No more machines of this type are available - please try again later.");
            }

            String userName = getUsername(icatUrl, sessionId);

            MachineUser machineUser = new MachineUser();
            machineUser.setUserName(userName);
            machineUser.setType("PRIMARY");
            machineUser.setWebsockifyToken(UUID.randomUUID().toString());
            machineUser.setMachine(machine);
            database.persist(machineUser);

            com.stfc.useroffice.webservice.UserOfficeWebService_Service service = new com.stfc.useroffice.webservice.UserOfficeWebService_Service();
            com.stfc.useroffice.webservice.UserOfficeWebService port = service.getUserOfficeWebServicePort();
            String fedId = port.getFedIdFromUserId(userName.replace("uows/", ""));

            SshClient sshClient = new SshClient(machine.getHost());
            sshClient.exec("add_primary_user " + fedId);
            sshClient.exec("add_websockify_token " + machineUser.getWebsockifyToken());

            machine.setScreenshot(Base64.getMimeDecoder().decode(sshClient.exec("get_screenshot")));
            machine.setCreatedAt(new Date());
            database.persist(machine);

            return machine.toResponse();
        } catch(DaaasException e) {
            return e.toResponse();
        } catch(Exception e){
            return new DaaasException(e.getMessage()).toResponse();
        }
    }

    @DELETE
    @Path("/machines/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteMachine(
        @PathParam("id") String id,
        @QueryParam("icatUrl") String icatUrl,
        @QueryParam("sessionId") String sessionId){

        logger.info("deleteMachine: user with sessionId " + sessionId + " is deleting machine with id " + id);

        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("id", id);

            Machine machine = (Machine) database.query("select machine from Machine machine where machine.id = :id", params).get(0);
            if(machine == null){
                throw new DaaasException("No such machine.");
            }
            if(!machine.getPrimaryUser().getUserName().equals(getUsername(icatUrl, sessionId))){
                throw new DaaasException("You are not allowed to delete this machine.");
            }
            cloudClient.deleteServer(machine.getId());
            database.remove(machine);
            return machine.toResponse();
    } catch(DaaasException e) {
            return e.toResponse();
        } catch(Exception e){
            return new DaaasException(e.getMessage()).toResponse();
        }
    }

    @PUT
    @Path("/machines/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response saveMachine(
        @PathParam("id") String id,
        @FormParam("icatUrl") String icatUrl,
        @FormParam("sessionId") String sessionId,
        @FormParam("name") String name){
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("id", id);

            Machine machine = (Machine) database.query("select machine from Machine machine where machine.id = :id", params).get(0);
            if(machine == null){
                throw new DaaasException("No such machine.");
            }
            if(!machine.getPrimaryUser().getUserName().equals(getUsername(icatUrl, sessionId))){
                throw new DaaasException("You are not allowed to save this machine.");
            }
            
            machine.setName(name);
            database.persist(machine);

            return machine.toResponse();
    } catch(DaaasException e) {
            return e.toResponse();
        } catch(Exception e){
            return new DaaasException(e.getMessage()).toResponse();
        }
    }

    @POST
    @Path("/machines/{id}/resolution")
    @Produces({MediaType.APPLICATION_JSON})
    public Response setMachineResolution(
        @PathParam("id") String id,
        @FormParam("icatUrl") String icatUrl,
        @FormParam("sessionId") String sessionId,
        @FormParam("width") int width,
        @FormParam("height") int height){

        logger.info("setMachineResolution: user with sessionId " + sessionId + " is setting the width/height of machine with id " + id + " to " + width + "x" + height);

        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("id", id);

            Machine machine = (Machine) database.query("select machine from Machine machine where machine.id = :id", params).get(0);
            if(machine == null){
                throw new DaaasException("No such machine.");
            }
            if(!machine.getPrimaryUser().getUserName().equals(getUsername(icatUrl, sessionId))){
                throw new DaaasException("You are not allowed to access this machine.");
            }

            new SshClient(machine.getHost()).exec("set_resolution " + width + " " + height);

            return machine.toResponse();
        } catch(DaaasException e) {
            return e.toResponse();
        } catch(Exception e){
            String message = e.getMessage();
            if(message == null){
                message = e.toString();
            }
            return new DaaasException(message).toResponse();
        }
    }

    @GET
    @Path("/machines/{id}/screenshot")
    @Produces("image/png")
    public Response getMachineScreenshot(
        @PathParam("id") String id,
        @QueryParam("icatUrl") String icatUrl,
        @QueryParam("sessionId") String sessionId){
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("id", id);

            Machine machine = (Machine) database.query("select machine from Machine machine where machine.id = :id", params).get(0);
            if(machine == null){
                throw new DaaasException("No such machine.");
            }

            String username = getUsername(icatUrl, sessionId);

            for(MachineUser user : machine.getMachineUsers()){
                if(user.getUserName().equals(username)){
                    CacheControl cacheControl = new CacheControl();
                    cacheControl.setNoStore(true);
                    return Response.ok(machine.getScreenshot()).cacheControl(cacheControl).build();
                }
            }

            throw new DaaasException("You are not allowed to access this machine.");
            
        } catch(DaaasException e) {
            return e.toResponse();
        } catch(Exception e){
            String message = e.getMessage();
            if(message == null){
                message = e.toString();
            }
            return new DaaasException(message).toResponse();
        }
    }

    @GET
    @Path("/machines/{id}/rdp")
    @Produces("application/x-rdp")
    public Response getRdpFile(
        @PathParam("id") String id,
        @QueryParam("icatUrl") String icatUrl,
        @QueryParam("sessionId") String sessionId){
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("id", id);

            Machine machine = (Machine) database.query("select machine from Machine machine where machine.id = :id", params).get(0);
            if(machine == null){
                throw new DaaasException("No such machine.");
            }

            String username = getUsername(icatUrl, sessionId);

            for(MachineUser user : machine.getMachineUsers()){
                if(user.getUserName().equals(username)){
                    StringBuffer out = new StringBuffer();

                    out.append("screen mode id:i:2\n");
                    out.append("desktopwidth:i:1024\n");
                    out.append("desktopheight:i:768\n");
                    out.append("session bpp:i:24\n");
                    out.append("compression:i:1\n");
                    out.append("keyboardhook:i:2\n");
                    out.append("displayconnectionbar:i:1\n");
                    out.append("disable wallpaper:i:1\n");
                    out.append("disable full window drag:i:1\n");
                    out.append("allow desktop composition:i:0\n");
                    out.append("allow font smoothing:i:0\n");
                    out.append("disable menu anims:i:1\n");
                    out.append("disable themes:i:0\n");
                    out.append("disable cursor setting:i:0\n");
                    out.append("bitmapcachepersistenable:i:1\n");
                    out.append("full address:s:" + machine.getHost() + "\n");
                    out.append("audiomode:i:2\n");
                    out.append("redirectprinters:i:0\n");
                    out.append("redirectsmartcard:i:0\n");
                    out.append("redirectcomports:i:0\n");
                    out.append("redirectsmartcards:i:0\n");
                    out.append("redirectclipboard:i:1\n");
                    out.append("redirectposdevices:i:0\n");
                    out.append("autoreconnection enabled:i:1\n");
                    out.append("authentication level:i:0\n");
                    out.append("prompt for credentials:i:1\n");
                    out.append("negotiate security layer:i:1\n");
                    out.append("remoteapplicationmode:i:0\n");
                    out.append("alternate shell:s:\n");
                    out.append("shell working directory:s:\n");
                    out.append("gatewayhostname:s:\n");
                    out.append("gatewayusagemethod:i:4\n");
                    out.append("gatewaycredentialssource:i:4\n");
                    out.append("gatewayprofileusagemethod:i:0\n");
                    out.append("precommand:s:\n");
                    out.append("promptcredentialonce:i:1\n");
                    out.append("drivestoredirect:s:\n");

                    return Response.ok(out.toString()).header("Content-Disposition", "attachment; filename='" + machine.getHost() + ".rdp'").build();
                }
            }

            throw new DaaasException("You are not allowed to access this machine.");
            
        } catch(DaaasException e) {
            return e.toResponse();
        } catch(Exception e){
            String message = e.getMessage();
            if(message == null){
                message = e.toString();
            }
            return new DaaasException(message).toResponse();
        }
    }

    @POST
    @Path("/machines/{id}/share")
    @Produces({MediaType.APPLICATION_JSON})
    public Response shareMachine(
        @PathParam("id") String id,
        @FormParam("icatUrl") String icatUrl,
        @FormParam("sessionId") String sessionId,
        @FormParam("userNames") String userNames) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("id", id);

            Machine machine = (Machine) database.query("select machine from Machine machine where machine.id = :id", params).get(0);
            if(machine == null){
                throw new DaaasException("No such machine.");
            }
            if(!machine.getPrimaryUser().getUserName().equals(getUsername(icatUrl, sessionId))){
                throw new DaaasException("You are not allowed to access this machine.");
            }

            String[] userNamesList = userNames.split("\\s*,\\s*");
            EntityList<MachineUser> newMachineUsers = new EntityList<MachineUser>();

            SshClient sshClient = new SshClient(machine.getHost());

            com.stfc.useroffice.webservice.UserOfficeWebService_Service service = new com.stfc.useroffice.webservice.UserOfficeWebService_Service();
            com.stfc.useroffice.webservice.UserOfficeWebService port = service.getUserOfficeWebServicePort();

            for(MachineUser machineUser : machine.getMachineUsers()){
                if(machineUser.getType().equals("PRIMARY")){
                    newMachineUsers.add(machineUser);
                }
            }

            for(String userName : userNamesList){
                if(userName.equals("")){
                    continue;
                }

                boolean isExistingUser = false;

                for(MachineUser machineUser : machine.getMachineUsers()){
                    if(machineUser.getUserName().equals(userName)){
                        newMachineUsers.add(machineUser);
                        isExistingUser = true;
                        break;
                    }
                }

                if(!isExistingUser){
                    MachineUser newMachineUser = new MachineUser();
                    newMachineUser.setUserName(userName);
                    newMachineUser.setType("SECONDARY");
                    newMachineUser.setMachine(machine);
                    newMachineUser.setWebsockifyToken(UUID.randomUUID().toString());
                    database.persist(newMachineUser);

                    String fedId = port.getFedIdFromUserId(userName.replace("uows/", ""));

                    sshClient.exec("add_secondary_user " + fedId);
                    sshClient.exec("add_websockify_token " + newMachineUser.getWebsockifyToken());
                }
            }

            for(MachineUser machineUser : machine.getMachineUsers()){
                boolean isRemoved = true;
                for(String userName : userNamesList){
                    if(machineUser.getUserName().equals(userName) || machineUser.getType().equals("PRIMARY")){
                        isRemoved = false;
                    }
                }

                if(isRemoved){
                    String fedId = port.getFedIdFromUserId(machineUser.getUserName().replace("uows/", ""));
                    sshClient.exec("remove_secondary_user " + fedId);
                    sshClient.exec("remove_websockify_token " + machineUser.getWebsockifyToken());
                }
            }

            machine.setMachineUsers(newMachineUsers);

            database.persist(machine);

            return machine.toResponse();
        } catch(DaaasException e) {
            return e.toResponse();
        } catch(Exception e){
            String message = e.getMessage();
            if(message == null){
                message = e.toString();
            }
            return new DaaasException(message).toResponse();
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

    @GET
    @Path("/machineTypes/{id}/logo")
    public Response getMachineTypeLogo(
        @PathParam("id") Integer id) {

        try {
            MachineType machineType = (MachineType) database.query("select machineType from MachineType machineType where machineType.id = " + id).get(0);

            return Response.ok(machineType.getLogoData(), machineType.getLogoMimeType()).build();
        } catch(DaaasException e) {
            return e.toResponse();
        } catch(Exception e) {
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

}
