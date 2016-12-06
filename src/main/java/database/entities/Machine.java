/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Images
 * and open the template in the editor.
 */
package org.icatproject.topcatdoiplugin.database.entities;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.icatproject.topcatdaaasplugin.responseproducer.Entity;

/**
 *
 * @author elz24996
 */

public class Machine extends Entity {
    
    private String id;
    private String name;
    private String state;
    private String host;
    
    public Machine(CloudClient cloudClient){
        super(cloudClient);
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        try {
            return Websockify.getInstance().getToken("elz24996", getHost());
        } catch(Exception e) {
            return e.toString();
        }
    }
    
    
    public JsonObjectBuilder toJsonObjectBuilder(){
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("name", getName());
        out.add("state", getState());
        out.add("host", getHost());
        out.add("websockifyToken", getWebsockifyToken());
        return out;
    }
    
}
