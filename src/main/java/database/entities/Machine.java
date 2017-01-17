/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Images
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.database.entities;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.icatproject.topcatdaaasplugin.Entity;

import javax.persistence.FetchType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author elz24996
 */

@javax.persistence.Entity
@Table(name = "MACHINE")
@XmlRootElement
public class Machine extends Entity {
    
    @Id
    @Column(name = "ID", nullable = false)
    private String id;

    @Column(name = "OWNER", nullable = true)
    private String owner;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "STATE", nullable = false)
    private String state;

    @Column(name = "HOST", nullable = false)
    private String host;

    @Column(name = "WEBSOCKIFY_TOKEN", nullable = false)
    private String websockifyToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "MACHINE_TYPE_ID")
    private MachineType machineType;
    
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
        this.websockifyToken = websockifyToken;
    }

    public MachineType getMachineType() {
        return machineType;
    }

    public void setMachineType(MachineType machineType) {
        this.machineType = machineType;
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
