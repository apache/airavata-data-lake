package org.apache.airavata.datalake.metadata.backend.neo4j.model.relationships;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(type = "SHARED_WITH")
public class SharedWith extends Relationship {


    public SharedWith() {

    }

    @Property(name = "permission_type")
    private String permissionType;

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

}