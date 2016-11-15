/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.cloudclient;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author elz24996
 */
public class Template extends Entity {
    
    
    private String id;
    private String name;

    
    public Template(CloudClient cloudClient){
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
    
    public JsonObjectBuilder toJsonObjectBuilder(){
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("name", getName());
        return out;
    }
}
