package org.apache.airavata.datalake.orchestrator.registry.persistance.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "OWNERSHIP_ENTITY")
@EntityListeners(AuditingEntityListener.class)
public class OwnershipEntity {

    @Id
    private String id;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "PERMISSION_ID")
    private String permissionId;

    @ManyToOne
    @JoinColumn(name = "DATA_ORCHESTRATOR_ENTITY_ID")
    private DataOrchestratorEntity dataOrchestratorEntity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    public DataOrchestratorEntity getDataOrchestratorEntity() {
        return dataOrchestratorEntity;
    }

    public void setDataOrchestratorEntity(DataOrchestratorEntity dataOrchestratorEntity) {
        this.dataOrchestratorEntity = dataOrchestratorEntity;
    }
}
