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
        try {
            if(!isMachineTypeAllowed(icatUrl, sessionId, machineTypeId)){
                 throw new DaaasException("You are not allowed to create this machine type.");
            }
            Machine machine = machinePool.aquireMachine(machineTypeId);
            database.persist(machine);

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
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("id", id);

            Machine machine = (Machine) database.query("select machine from Machine machine where machine.id = :id", params).get(0);
            if(machine == null){
                throw new DaaasException("No such machine.");
            }
            if(!machine.getPrimaryUser().getUserName().equals(getUsername(icatUrl, sessionId))){
                throw new DaaasException("You are not allowed to delete this machine type.");
            }
            cloudClient.deleteMachine(machine.getId());
            database.remove(machine);
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
    public Response setMachineScreenshot(
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
                    return Response.ok(machine.getScreenshot()).build();
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
    @Path("/sshTest")
    @Produces({MediaType.TEXT_PLAIN})
    public Response sshTest(){
        try {
            SshClient sshClient = new SshClient("130.246.186.17");

            return Response.ok().entity(sshClient.exec("get_screenshot")).build();
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




 // case "${cmd[0]}" in
    //   "set_resolution")
    //     arg1=${cmd[1]//[^0-9]/}
    //     arg2=${cmd[2]//[^0-9]/}
    //     $SCRIPTS_DIR/set_resolution.sh $arg1 $arg2
    //     ;;
    //   get_screenshot)
    //     $SCRIPTS_DIR/get_screenshot.sh
    //     ;;
    //   add_primary_user)
    //     $SCRIPTS_DIR/add_primary_user.sh ${cmd[1]}
    //     ;;
    //   add_secondary_user)
    //     $SCRIPTS_DIR/add_secondary_user.sh ${cmd[1]}
    //     ;;
    //   remove_secondary_user)
    //     $SCRIPTS_DIR/remove_secondary_user.sh ${cmd[1]}
    //     ;;
    //   get_last_activity)
    //     $SCRIPTS_DIR/get_last_activity.sh
    //     ;;
    //   add_websockify_token)
    //     $SCRIPTS_DIR/add_websockify_token.sh ${cmd[1]}
    //     ;;
    //   remove_websockify_token)
    //     $SCRIPTS_DIR/remove_websockify_token.sh ${cmd[1]}
    //     ;;
    //   set_machine_type)
    //     $SCRIPTS_DIR/set_machine_type.sh ${cmd[1]}
    //     ;;
    //   *)
    //     echo "Unrecognised command"
    //     exit 1
    //     ;;
    // esac

    // public byte[] getScreenshot()  throws Exception {
    //     return Base64.getDecoder().decode(new SshClient(this.host).exec("get_screenshot"));
    // }

    // public void setResolution(int width, int height)  throws Exception {
    //     new SshClient(getHost()).exec("set_resolution " + width + " " + height);
    // }

    // public void addPrimaryUser(String username) throws Exception {
    //     new SshClient(getHost()).exec("add_primary_user " + username);
    // }

    // public void addSecondaryUser(String username) throws Exception {
    //     new SshClient(getHost()).exec("add_secondary_user " + username);
    // }

    // public void removeSecondaryUser(String username) throws Exception {
    //     new SshClient(getHost()).exec("remove_secondary_user " + username);
    // }

    // public Date getLastActivity() throws Exception {
    //     return new SimpleDateFormat().parse(new SshClient(getHost()).exec("get_last_activity"));
    // }

    // public void addWebsockifyToken(String token) throws Exception {
    //     new SshClient(getHost()).exec("add_websockify_token " + token);
    // }

    // public void removeWebsockifyToken(String token) throws Exception {
    //     new SshClient(getHost()).exec("remove_websockify_token " + token);
    // }

    // public void contextualize() throws Exception {
    //     //addPrimaryUser(getOwner());
    //     //addWebsockifyToken(getWebsockifyToken());
    // }
