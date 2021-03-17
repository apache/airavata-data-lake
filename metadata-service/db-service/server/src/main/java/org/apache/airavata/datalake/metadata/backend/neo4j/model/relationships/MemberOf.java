package org.apache.airavata.datalake.metadata.backend.neo4j.model.relationships;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(type = "MEMBER_OF")
public class MemberOf extends Relationship {

    @Property(name = "membership_type")
    private String membershipType;

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }
}
