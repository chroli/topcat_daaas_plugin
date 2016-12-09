package org.icatproject.topcatdaaasplugin.database.entities;

import java.util.List;
import java.io.Serializable;
import java.util.Date;

import javax.json.Json;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.json.JsonObjectBuilder;


import org.icatproject.topcatdaaasplugin.Entity;
import org.icatproject.topcatdaaasplugin.EntityList;

@javax.persistence.Entity
@Table(name = "MACHINETYPE")
@XmlRootElement
public class MachineType extends Entity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "IMAGE_ID", nullable = false)
    private String imageId;

    @Column(name = "FLAVOR_ID", nullable = false)
    private String flavorId;

    @Column(name = "AVAILABILITY_ZONE", nullable = false)
    private String availabilityZone;

    @Column(name = "POOL_SIZE", nullable = false)
    private Integer poolSize;

    @Column(name = "PERSONALITY", nullable = false)
    private String personality;

    @Column(name = "CREATED_AT", nullable=false, updatable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "machineType", orphanRemoval = true)
    private List<MachineTypeScope> machineTypeScopes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getFlavorId() {
        return flavorId;
    }

    public void setFlavorId(String flavorId) {
        this.flavorId = flavorId;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public Integer getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(Integer poolSize) {
        this.poolSize = poolSize;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
       this.personality = personality;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public EntityList<MachineTypeScope> getMachineTypeScopes(){
        EntityList<MachineTypeScope> out = new EntityList<MachineTypeScope>();
        for(MachineTypeScope machineTypeScope : machineTypeScopes){
            out.add(machineTypeScope);
        }
        return out;
    }

    public void setMachineTypeScopes(List<MachineTypeScope> machineTypeScopes) {
        this.machineTypeScopes = machineTypeScopes;
    }

    @PrePersist
    private void createAt() {
        this.createdAt = new Date();
    }

    public JsonObjectBuilder toJsonObjectBuilder(){
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("name", getName());
        out.add("imageId", getImageId());
        out.add("flavorId", getFlavorId());
        out.add("availabilityZone", getAvailabilityZone());
        out.add("poolSize", getPoolSize());
        out.add("personality", getPersonality());
        out.add("createAt", getCreatedAt().toString());
        out.add("scopes", getMachineTypeScopes().toJsonArrayBuilder());
        return out;
    }

}


