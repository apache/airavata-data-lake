package org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.nodes;

import org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.relationships.HasRole;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Role extends Entity {

    @Property(name = "scope")
    private String scope;


    @Relationship(type = "HAS_ROLE", direction = Relationship.INCOMING)
    private HasRole hasRole;

    public Role() {
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }


    public HasRole getHasRole() {
        return hasRole;
    }

    public void setHasRole(HasRole hasRole) {
        this.hasRole = hasRole;
    }
}
