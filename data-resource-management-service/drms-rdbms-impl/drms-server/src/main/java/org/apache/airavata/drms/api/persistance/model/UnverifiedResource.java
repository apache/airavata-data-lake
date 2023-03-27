package org.apache.airavata.drms.api.persistance.model;


import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "UNVERIFIED_RESOURCE")
@EntityListeners(AuditingEntityListener.class)
public class UnverifiedResource {

    @Id
    @Column(name="ID")
    private String id;

    @Column(name="TENANT_ID",nullable = false)
    private String tenantId;

    @Column(name="RESOURCE_PATH")
    private String path;

    @Column(name="ERROR_CODE")
    private String errorCode;

    @Column(name="ERROR_DISCRIPTION")
    private String errorDiscription;

    @Column(name="TYPE")
    private String type;

    @Column(name="UNVERIFIED_OWNER")
    private String unverifiedAssociatedOwner;


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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDiscription() {
        return errorDiscription;
    }

    public void setErrorDiscription(String errorDiscription) {
        this.errorDiscription = errorDiscription;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnverifiedAssociatedOwner() {
        return unverifiedAssociatedOwner;
    }

    public void setUnverifiedAssociatedOwner(String unverifiedAssociatedOwner) {
        this.unverifiedAssociatedOwner = unverifiedAssociatedOwner;
    }
}
