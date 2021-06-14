package org.apache.airavata.datalake.orchestrator.registry.persistance;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * An entity class represents the task entity
 */
@Entity
@Table(name = "WORKFLOW_TASK_ENTITY")
@EntityListeners(AuditingEntityListener.class)
public class WorkflowTaskEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "workflow_entity_id")
    private WorkflowEntity workflowEntity;


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

    @Lob
    private String error;

    private int errorCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WorkflowEntity getWorkflowEntity() {
        return workflowEntity;
    }

    public void setWorkflowEntity(WorkflowEntity workFlowEntity) {
        this.workflowEntity = workFlowEntity;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
