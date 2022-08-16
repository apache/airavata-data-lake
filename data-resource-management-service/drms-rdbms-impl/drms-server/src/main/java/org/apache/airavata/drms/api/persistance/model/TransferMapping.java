package org.apache.airavata.drms.api.persistance.model;


import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "TRANSFER_MAPPING")
@EntityListeners(AuditingEntityListener.class)
public class TransferMapping {


    @Id
    @Column(name="ID")
    private String id;

    @ManyToOne
    @JoinColumn(name = "source_resource_id")
    private Resource source;

    @ManyToOne
    @JoinColumn(name = "destination_resource_id")
    private Resource destination;

    @Column(name="SCOPE")
    private String scope;

    @Column(name="OWNER_ID")
    private String ownerId;


    public Resource getSource() {
        return source;
    }

    public void setSource(Resource source) {
        this.source = source;
    }

    public Resource getDestination() {
        return destination;
    }

    public void setDestination(Resource destination) {
        this.destination = destination;
    }


    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
