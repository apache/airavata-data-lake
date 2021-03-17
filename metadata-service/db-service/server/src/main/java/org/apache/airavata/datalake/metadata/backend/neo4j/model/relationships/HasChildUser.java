package org.apache.airavata.datalake.metadata.backend.neo4j.model.relationships;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(type = "HAS_CHILD_USER")
public class HasChildUser extends Relationship {

    @Property(name = "user_type")
    private String userType;

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
