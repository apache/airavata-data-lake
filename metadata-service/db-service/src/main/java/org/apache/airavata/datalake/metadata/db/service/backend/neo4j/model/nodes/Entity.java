package org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.nodes;

import org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.relationships.Has;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;

public abstract class Entity {
    @Id
    @GeneratedValue
    private Long id;

    @Properties(prefix = "external_id")
    private Map<String,String > externalIds = new HashMap<>();

    @Property(name = "created_at")
    private long createdAt = System.currentTimeMillis();

    @Property(name = "last_modified_at")
    private long lastModifiedAt = System.currentTimeMillis();

    @Property(name = "source")
    private String source;

    @Properties(prefix = "custom")
    private Map<String, String> properties = new HashMap<>();

    @Property(name = "primary_external_key")
    private String primaryExternalKey;

    @Relationship(type = "HAS", direction = Relationship.INCOMING)
    private Has tenantInPointer;

    @Property(name = "tenant_id")
    private String tenantId;

    public Long getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(long lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(Map<String, String> externalIds) {
        this.externalIds = externalIds;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPrimaryExternalKey() {
        return primaryExternalKey;
    }

    public void setPrimaryExternalKey(String primaryExternalKey) {
        this.primaryExternalKey = primaryExternalKey;
    }

    public Has getTenantInPointer() {
        return tenantInPointer;
    }

    public void setTenantInPointer(Has tenantInPointer) {
        this.tenantInPointer = tenantInPointer;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
