package org.icatproject.topcatdaaasplugin.database.entities;

import org.icatproject.topcatdaaasplugin.Entity;
import org.icatproject.topcatdaaasplugin.EntityList;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.List;

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

    @Column(name = "DESCRIPTION", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "LOGO_MIME_TYPE")
    private String logoMimeType;

    @Lob
    @Column(name = "LOGO_DATA")
    private byte[] logoData;

    @Column(name = "IMAGE_ID", nullable = false)
    private String imageId;

    @Column(name = "FLAVOR_ID", nullable = false)
    private String flavorId;

    @Column(name = "AVAILABILITY_ZONE", nullable = false)
    private String availabilityZone;

    @Column(name = "POOL_SIZE", nullable = false)
    private Integer poolSize;

    @Column(name = "AQUILON_ARCHETYPE")
    private String aquilonArchetype;

    @Column(name = "AQUILON_DOMAIN")
    private String aquilonDomain;

    @Column(name = "AQUILON_PERSONALITY")
    private String aquilonPersonality;

    @Column(name = "AQUILON_SANDBOX")
    private String aquilonSandbox;

    @Column(name = "AQUILON_OS_VERSION")
    private String aquilonOSVersion;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "machineType", orphanRemoval = true)
    private List<MachineTypeScope> machineTypeScopes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "machineType", orphanRemoval = true)
    private List<Machine> machines;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoMimeType() {
        return logoMimeType;
    }

    public void setLogoMimeType(String logoMimeType) {
        this.logoMimeType = logoMimeType;
    }

    public byte[] getLogoData() {
        return logoData;
    }

    public void setLogoData(byte[] logoData) {
        this.logoData = logoData;
    }

    public String getLogoMd5() {
        if (getLogoData() != null && getLogoData().length > 0) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                byte[] md5 = messageDigest.digest(getLogoData());
                return Base64.getEncoder().encodeToString(md5);
            } catch (Exception e) {
            }
        }
        return "";
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

    public void setAquilonArchetype(String aquilonArchetype) {
        this.aquilonArchetype = aquilonArchetype;
    }

    public String getAquilonArchetype() {
        return aquilonArchetype;
    }

    public String getAquilonDomain() {
        return aquilonDomain;
    }

    public void setAquilonDomain(String aquilonDomain) {
        this.aquilonDomain = aquilonDomain;
    }

    public String getAquilonPersonality() {
        return aquilonPersonality;
    }

    public void setAquilonPersonality(String aquilonPersonality) {
        this.aquilonPersonality = aquilonPersonality;
    }

    public String getAquilonSandbox() {
        return aquilonSandbox;
    }

    public void setAquilonSandbox(String aquilonSandbox) {
        this.aquilonSandbox = aquilonSandbox;
    }

    public String getAquilonOSVersion() {
        return aquilonOSVersion;
    }

    public void setAquilonOSVersion(String aquilonOSVersion) {
        this.aquilonOSVersion = aquilonOSVersion;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public EntityList<MachineTypeScope> getMachineTypeScopes() {
        EntityList<MachineTypeScope> out = new EntityList<MachineTypeScope>();
        for (MachineTypeScope machineTypeScope : machineTypeScopes) {
            out.add(machineTypeScope);
        }
        return out;
    }

    public void setMachineTypeScopes(List<MachineTypeScope> machineTypeScopes) {
        this.machineTypeScopes = machineTypeScopes;
    }

    public EntityList<Machine> getMachines() {
        EntityList<Machine> out = new EntityList<Machine>();
        for (Machine machine : machines) {
            out.add(machine);
        }
        return out;
    }

    public void setMachines(List<Machine> machines) {
        this.machines = machines;
    }

    @PrePersist
    private void createAt() {
        this.createdAt = new Date();
    }

    public JsonObjectBuilder toJsonObjectBuilder() {
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("name", getName());
        out.add("description", getDescription());

        if (getLogoMimeType() != null) {
            out.add("logoMimeType", getLogoMimeType());
        }

        if (!getLogoMd5().equals("")) {
            out.add("logoMd5", getLogoMd5());
        }

        out.add("imageId", getImageId());
        out.add("flavorId", getFlavorId());
        out.add("availabilityZone", getAvailabilityZone());
        out.add("poolSize", getPoolSize());
        out.add("aquilonArchetype", getAquilonArchetype());
        out.add("aquilonDomain", getAquilonDomain());
        out.add("aquilonPersonality", getAquilonPersonality());
        out.add("aquilonSandbox", getAquilonSandbox());
        out.add("aquilonOSVersion", getAquilonOSVersion());
        out.add("createAt", getCreatedAt().toString());
        out.add("scopes", getMachineTypeScopes().toJsonArrayBuilder());
        return out;
    }

}


