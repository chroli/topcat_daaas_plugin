package org.icatproject.topcatdaaasplugin.cloudclient.entities;

import org.icatproject.topcatdaaasplugin.Entity;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class Flavor extends Entity {

    private String id;
    private String name;
    private Integer cpus;
    private Integer disk;
    private Integer ram;

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

    public Integer getCpus() {
        return cpus;
    }

    public void setCpus(Integer cpus) {
        this.cpus = cpus;
    }

    public Integer getDisk() {
        return disk;
    }

    public void setDisk(Integer disk) {
        this.disk = disk;
    }

    public Integer getRam() {
        return ram;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public JsonObjectBuilder toJsonObjectBuilder() {
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("name", getName());
        out.add("cpus", getCpus());
        out.add("disk", getDisk());
        out.add("ram", getRam());
        return out;
    }

}