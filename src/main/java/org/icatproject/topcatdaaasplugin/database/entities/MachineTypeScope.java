package org.icatproject.topcatdaaasplugin.database.entities;


import org.icatproject.topcatdaaasplugin.Entity;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;


@javax.persistence.Entity
@Table(name = "MACHINETYPESCOPE")
@XmlRootElement
public class MachineTypeScope extends Entity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "QUERY", columnDefinition = "text", nullable = false)
    private String query;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MACHINE_TYPE_ID")
    private MachineType machineType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public MachineType getMachineType() {
        return machineType;
    }

    public void setMachineType(MachineType machineType) {
        this.machineType = machineType;
    }


    public JsonObjectBuilder toJsonObjectBuilder() {
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("query", getQuery());
        return out;
    }

}


