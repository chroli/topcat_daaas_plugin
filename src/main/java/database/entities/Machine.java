/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Images
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.database.entities;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.icatproject.topcatdaaasplugin.Entity;

import javax.persistence.FetchType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.Base64;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.icatproject.topcatdaaasplugin.SshClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author elz24996
 */

@javax.persistence.Entity
@Table(name = "MACHINE")
@XmlRootElement
public class Machine extends Entity {
    private static final Logger logger = LoggerFactory.getLogger(Machine.class);
    
    @Id
    @Column(name = "ID", nullable = false)
    private String id;

    @Column(name = "OWNER", nullable = true)
    private String owner;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "STATE", nullable = false)
    private String state;

    @Column(name = "HOST", nullable = false)
    private String host;

    @Column(name = "WEBSOCKIFY_TOKEN", nullable = false)
    private String websockifyToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "MACHINE_TYPE_ID")
    private MachineType machineType;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
    
    public String getWebsockifyToken(){
        //return Websockify.getInstance().getToken("elz24996", getHost());
        return websockifyToken;
    }

    public void setWebsockifyToken(String websockifyToken) {
        this.websockifyToken = websockifyToken;
    }

    public MachineType getMachineType() {
        return machineType;
    }

    public void setMachineType(MachineType machineType) {
        this.machineType = machineType;
    }
    
    public JsonObjectBuilder toJsonObjectBuilder(){
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("owner", getOwner());
        out.add("name", getName());
        out.add("state", getState());
        out.add("host", getHost());
        out.add("websockifyToken", getWebsockifyToken());
        return out;
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

    public byte[] getScreenshot()  throws Exception {
        return Base64.getDecoder().decode(new SshClient(this.host).exec("get_screenshot"));
    }

    public void setResolution(int width, int height)  throws Exception {
        new SshClient(getHost()).exec("set_resolution " + width + " " + height);
    }

    public void addPrimaryUser(String username) throws Exception {
        new SshClient(getHost()).exec("add_primary_user " + username);
    }

    public void addSecondaryUser(String username) throws Exception {
        new SshClient(getHost()).exec("add_secondary_user " + username);
    }

    public void removeSecondaryUser(String username) throws Exception {
        new SshClient(getHost()).exec("remove_secondary_user " + username);
    }

    public Date getLastActivity() throws Exception {
        return new SimpleDateFormat().parse(new SshClient(getHost()).exec("get_last_activity"));
    }

    public void addWebsockifyToken(String token) throws Exception {
        new SshClient(getHost()).exec("add_websockify_token " + token);
    }

    public void removeWebsockifyToken(String token) throws Exception {
        new SshClient(getHost()).exec("remove_websockify_token " + token);
    }

    public void contextualize() throws Exception {
        addPrimaryUser(getOwner());
        addWebsockifyToken(getWebsockifyToken());
    }
    
}
