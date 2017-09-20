package org.icatproject.topcatdaaasplugin.database.entities;

import org.icatproject.topcatdaaasplugin.Entity;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@javax.persistence.Entity
@Table(name = "MACHINEUSER")
@XmlRootElement
public class MachineUser extends Entity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USER_NAME", nullable = false)
    private String userName;

    @Column(name = "TYPE", nullable = false)
    private String type;

    @Column(name = "WEBSOCKIFY_TOKEN", nullable = false)
    private String websockifyToken;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MACHINE_ID")
    private Machine machine;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWebsockifyToken() {
        return websockifyToken;
    }

    public void setWebsockifyToken(String websockifyToken) {
        this.websockifyToken = websockifyToken;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    @PrePersist
    private void createAt() {
        this.createdAt = new Date();
    }

    public JsonObjectBuilder toJsonObjectBuilder() {
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("userName", getUserName());
        out.add("type", getType());
        out.add("websockifyToken", getWebsockifyToken());
        out.add("createdAt", getCreatedAt().toString());
        return out;
    }

}


