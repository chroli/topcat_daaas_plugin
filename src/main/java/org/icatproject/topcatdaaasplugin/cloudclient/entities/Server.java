package org.icatproject.topcatdaaasplugin.cloudclient.entities;

import org.icatproject.topcatdaaasplugin.Entity;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class Server extends Entity {

    private String id;
    private String host;
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JsonObjectBuilder toJsonObjectBuilder() {
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        if (getHost() != null) {
            out.add("host", getHost());
        }
        if (getStatus() != null) {
            out.add("status", getStatus());
        }
        return out;
    }

}