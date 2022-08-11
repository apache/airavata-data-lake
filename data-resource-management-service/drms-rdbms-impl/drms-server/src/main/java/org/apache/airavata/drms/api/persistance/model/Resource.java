package org.apache.airavata.drms.api.persistance.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "resource")
@EntityListeners(AuditingEntityListener.class)
public class Resource {

    @Id
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column
    private String parentResourceId;

    @Column
    private String type;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "property", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<ResourceProperty> resourceProperty;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "transferMapping", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<TransferMapping> transferMapping;


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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<ResourceProperty> getResourceProperty() {
        return resourceProperty;
    }

    public void setResourceProperty(Set<ResourceProperty> resourceProperty) {
        this.resourceProperty = resourceProperty;
    }

    public Set<TransferMapping> getTransferMapping() {
        return transferMapping;
    }

    public void setTransferMapping(Set<TransferMapping> transferMapping) {
        this.transferMapping = transferMapping;
    }
}
