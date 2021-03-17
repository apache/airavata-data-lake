package org.apache.airavata.datalake.metadata.backend.neo4j.model.relationships;

import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Entity;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;

public abstract class Relationship {
    @Id
    @GeneratedValue
    private Long id;


    @Property(name = "created_at")
    private long createdAt = System.currentTimeMillis();

    @Property(name = "last_modified_at")
    private long lastModifiedAt = System.currentTimeMillis();


    @StartNode
    private Entity startEntity;

    @EndNode
    private Entity endEntity;


    @Properties(prefix = "custom")
    private Map<String, String> properties = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


    public Entity getStartEntity() {
        return startEntity;
    }

    public void setStartEntity(Entity startEntity) {
        this.startEntity = startEntity;
    }

    public Entity getEndEntity() {
        return endEntity;
    }

    public void setEndEntity(Entity endEntity) {
        this.endEntity = endEntity;
    }
}
