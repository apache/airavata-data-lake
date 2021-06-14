package org.apache.airavata.datalake.orchestrator.registry.persistance;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * An workflow class that represents the workflow entity
 */
@Entity
@Table(name = "WORKFLOW_ENTITY")
@EntityListeners(AuditingEntityListener.class)
public class WorkflowEntity {

    @Id
    private String id;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdAt;


    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date lastModifiedAt;

    @Column(nullable = false)
    private String status;


    @ManyToOne
    @JoinColumn(name = "dataorchestrator_entity_id")
    private DataOrchestratorEntity dataOrchestratorEntity;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "workflowEntity", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<WorkflowTaskEntity> workflowTaskEntities;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(Date lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DataOrchestratorEntity getDataOrchestratorEntity() {
        return dataOrchestratorEntity;
    }

    public void setDataOrchestratorEntity(DataOrchestratorEntity dataOrchestratorEntity) {
        this.dataOrchestratorEntity = dataOrchestratorEntity;
    }

    public Set<WorkflowTaskEntity> getWorkflowTaskEntities() {
        return workflowTaskEntities;
    }

    public void setWorkflowTaskEntities(Set<WorkflowTaskEntity> workflowTaskEntities) {
        this.workflowTaskEntities = workflowTaskEntities;
    }
}
