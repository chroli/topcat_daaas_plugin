/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.cloudclient;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import org.icatproject.topcatdaaasplugin.Websockify;

/**
 *
 * @author elz24996
 */

public class Machine extends Entity {
    
    private Integer id;
    private String name;
    private String groupName;
    private String state;
    private String host;
    
    public Machine(CloudClient cloudClient){
        super(cloudClient);
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
        try {
            return Websockify.getInstance().getToken(cloudClient.getUser().getUsername(), getHost());
        } catch(Exception e) {
            return e.toString();
        }
    }
    
    
    public JsonObjectBuilder toJsonObjectBuilder(){
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("name", getName());
        out.add("state", getState());
        out.add("groupName", getGroupName());
        out.add("host", getHost());
        out.add("websockifyToken", getWebsockifyToken());
        return out;
    }
    
}
