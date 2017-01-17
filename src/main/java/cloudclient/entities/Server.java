package org.icatproject.topcatdaaasplugin.cloudclient.entities;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.icatproject.topcatdaaasplugin.Entity;

public class Server extends Entity {

    private String id;
    private String host;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

	public JsonObjectBuilder toJsonObjectBuilder(){
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("host", getHost());
        return out;
    }

}