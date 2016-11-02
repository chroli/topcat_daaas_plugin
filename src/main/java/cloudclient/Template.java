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
    
    
    private Integer id;
    private String name;
    private String description;
    private Integer cpuCount;
    private Integer memoryAllocation;
    
    public Template(CloudClient cloudClient){
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(Integer cpuCount) {
        this.cpuCount = cpuCount;
    }

    public Integer getMemoryAllocation() {
        return memoryAllocation;
    }

    public void setMemoryAllocation(Integer memoryAllocation) {
        this.memoryAllocation = memoryAllocation;
    }
    
    public JsonObjectBuilder toJsonObjectBuilder(){
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("name", getName());
        out.add("description", getDescription());
        out.add("cpuCount", getCpuCount());
        out.add("memoryAllocation", getMemoryAllocation());
        return out;
    }
}
