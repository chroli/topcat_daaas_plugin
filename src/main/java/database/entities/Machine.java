/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Images
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.database.entities;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.icatproject.topcatdaaasplugin.Entity;

/**
 *
 * @author elz24996
 */

public class Machine extends Entity {
    
    private String id;
    private String owner;
    private String name;
    private String state;
    private String host;
    private String websockifyToken;
    
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
        this.host = websockifyToken;
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
    
}
