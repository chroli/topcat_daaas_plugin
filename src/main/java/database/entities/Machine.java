/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Images
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.database.entities;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.icatproject.topcatdaaasplugin.Entity;
import org.icatproject.topcatdaaasplugin.EntityList;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.icatproject.topcatdaaasplugin.SshClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.util.Base64;

/**
 *
 * @author elz24996
 */

@javax.persistence.Entity
@Table(name = "MACHINE")
@XmlRootElement
public class Machine extends Entity {
    private static final Logger logger = LoggerFactory.getLogger(Machine.class);
    
    @Id
    @Column(name = "ID", nullable = false)
    private String id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "STATE", nullable = false)
    private String state;

    @Column(name = "HOST", nullable = false)
    private String host;

    @Lob
    @Column(name = "SCREENSHOT")
    private byte[] screenshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "MACHINE_TYPE_ID")
    private MachineType machineType;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "machine", orphanRemoval = true)
    private List<MachineUser> machineUsers;
    
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

    public byte[] getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(byte[] screenshot) {
        this.screenshot = screenshot;
    }

    public String getScreenshotMd5(){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] md5 = messageDigest.digest(getScreenshot());
            return Base64.getEncoder().encodeToString(md5);
        } catch(Exception e){
            return "";
        }
    }

    public MachineType getMachineType() {
        return machineType;
    }

    public void setMachineType(MachineType machineType) {
        this.machineType = machineType;
    }

    public EntityList<MachineUser> getMachineUsers(){
        EntityList<MachineUser> out = new EntityList<MachineUser>();
        for(MachineUser machineUser : machineUsers){
            out.add(machineUser);
        }
        return out;
    }

    public void setMachineUsers(List<MachineUser> machineUsers) {
        this.machineUsers = machineUsers;
    }

    public MachineUser getPrimaryUser(){
        for(MachineUser machineUser : getMachineUsers()){
            if(machineUser.getType().equals("PRIMARY")){
                return machineUser;
            }
        }
        return null;
    }
    
    public JsonObjectBuilder toJsonObjectBuilder(){
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("name", getName());
        out.add("state", getState());
        out.add("host", getHost());
        out.add("screenshotMd5", getScreenshotMd5());
        out.add("users", getMachineUsers().toJsonArrayBuilder());
        return out;
    }
    
}
