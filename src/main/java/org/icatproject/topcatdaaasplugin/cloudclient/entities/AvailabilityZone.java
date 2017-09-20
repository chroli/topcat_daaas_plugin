package org.icatproject.topcatdaaasplugin.cloudclient.entities;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.icatproject.topcatdaaasplugin.Entity;

public class AvailabilityZone extends Entity {

    private String name;
    private Boolean isAvailable;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

	public JsonObjectBuilder toJsonObjectBuilder(){
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("name", getName());
        out.add("isAvailable", getIsAvailable());
        return out;
    }

}