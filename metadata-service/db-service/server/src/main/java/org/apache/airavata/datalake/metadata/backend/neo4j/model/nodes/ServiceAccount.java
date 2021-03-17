package org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes;

import org.apache.airavata.datalake.metadata.backend.neo4j.model.relationships.Has;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class ServiceAccount extends Entity {

    @Property(name = "status")
    private String status;


    @Relationship(type = "HAS", direction = Relationship.INCOMING)
    private Has hasRole;

    public ServiceAccount() {

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
