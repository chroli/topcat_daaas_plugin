/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.cloudclient;

import java.math.BigDecimal;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author elz24996
 */
public class User extends Entity {

    private String username;
    
    public User(CloudClient cloudClient){
        super(cloudClient);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public JsonObjectBuilder toJsonObjectBuilder(){
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("username", getUsername());
        return out;
    }
    

}
