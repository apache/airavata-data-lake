package org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.relationships;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(type = "HAS_PARENT_GROUP")
public class HasParentGroup extends Relationship{

    @Property(name = "membership_type")
    private String membershipType;

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }
}
