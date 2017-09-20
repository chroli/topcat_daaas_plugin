package org.icatproject.topcatdaaasplugin.database.entities;


import java.io.Serializable;
import java.util.Date;

import javax.json.Json;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.json.JsonObjectBuilder;


import org.icatproject.topcatdaaasplugin.Entity;


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
    @JoinColumn(name= "MACHINE_TYPE_ID")
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


    public JsonObjectBuilder toJsonObjectBuilder(){
        JsonObjectBuilder out = Json.createObjectBuilder();
        out.add("id", getId());
        out.add("query", getQuery());
        return out;
    }

}


