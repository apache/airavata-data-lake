package org.apache.airavata.datalake.orchestrator.registry.persistance;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * DataOrchestrator entity
 */
@Entity
@Table(name = "DATAORCHESTRATOR_ENTITY")
@EntityListeners(AuditingEntityListener.class)
public class DataOrchestratorEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String resourceId;

    @Column(nullable = false)
    private String resourcePath;

    @Column(nullable = false)
    private String resourceName;

    @Column(nullable = false)
    private String resourceType;

    @Column(nullable = false)
    private Date occurredTime;


    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdAt;


    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date lastModifiedAt;

    @Column(nullable = false)
    private String eventStatus;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String ownerId;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String agentId;

    @Column(nullable = false)
    private String authToken;

    @Column(nullable = false)
    private String hostName;

    @Lob
    private String error;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "dataOrchestratorEntity", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<WorkflowEntity> workFlowEntities;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Date getOccurredTime() {
        return occurredTime;
    }

    public void setOccurredTime(Date occurredTime) {
        this.occurredTime = occurredTime;
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

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String status) {
        this.eventStatus = status;
    }

    public Set<WorkflowEntity> getWorkFlowEntities() {
        return workFlowEntities;
    }

    public void setWorkFlowEntities(Set<WorkflowEntity> workFlowEntities) {
        this.workFlowEntities = workFlowEntities;
    }


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
