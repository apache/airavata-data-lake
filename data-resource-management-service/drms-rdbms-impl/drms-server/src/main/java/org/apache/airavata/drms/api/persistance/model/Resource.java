package org.apache.airavata.drms.api.persistance.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "RESOURCE")
@EntityListeners(AuditingEntityListener.class)
public class Resource {

    @Id
    @Column(name="ID")
    private String id;

    @Column(name="TENANT_ID",nullable = false)
    private String tenantId;

    @Column(name="PARENT_RESOURCE_ID")
    private String parentResourceId;

    @Column(name="RESOURCE_TYPE")
    private String resourceType;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "resource", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<ResourceProperty> resourceProperty;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "source", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<TransferMapping> sourceTransferMapping;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "destination", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<TransferMapping> destinationTransferMapping;


    public String getParentResourceId() {
        return parentResourceId;
    }

    public void setParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String type) {
        this.resourceType = type;
    }

    public Set<ResourceProperty> getResourceProperty() {
        return resourceProperty;
    }

    public void setResourceProperty(Set<ResourceProperty> resourceProperty) {
        this.resourceProperty = resourceProperty;
    }

    public Set<TransferMapping> getSourceTransferMapping() {
        return sourceTransferMapping;
    }

    public void setSourceTransferMapping(Set<TransferMapping> transferMapping) {
        this.sourceTransferMapping = transferMapping;
    }

    public Set<TransferMapping> getDestinationTransferMapping() {
        return destinationTransferMapping;
    }

    public void setDestinationTransferMapping(Set<TransferMapping> destinationTransferMapping) {
        this.destinationTransferMapping = destinationTransferMapping;
    }
}
