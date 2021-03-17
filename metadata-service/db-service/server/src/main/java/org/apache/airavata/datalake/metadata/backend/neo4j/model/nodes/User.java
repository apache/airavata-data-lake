package org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes;

import org.apache.airavata.datalake.metadata.backend.neo4j.model.relationships.*;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class User extends Entity {
    @Property(name = "name")
    private String username;
    @Property(name = "first_name")
    private String firstName;
    @Property(name = "last_name")
    private String lastName;
    @Property(name = "email")
    private String email;
    @Property(name = "status")
    private String status;

    @Relationship(type = "BELONGS")
    private Belongs belongs;

    @Relationship(type = "HAS", direction = Relationship.INCOMING)
    private Has hasRole;

    @Relationship(type = "MEMBER_OF")
    private final Set<MemberOf> memberGroups = new HashSet<>();

    @Relationship(type = "HAS_CHILD_USER", direction = Relationship.INCOMING)
    private final Set<HasChildUser> groups = new HashSet<>();

    @Relationship(type = "SHARED_WITH", direction = Relationship.INCOMING)
    private final Set<SharedWith> sharedResources = new HashSet<>();

    @Relationship(type = "HAS_ACCESS")
    private final Set<HasAccess> accessibleResources = new HashSet<>();

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Belongs getBelongs() {
        return belongs;
    }

    public void setBelongs(Belongs belongs) {
        this.belongs = belongs;
    }

    public Has getHasRole() {
        return hasRole;
    }

    public void setHasRole(Has hasRole) {
        this.hasRole = hasRole;
    }

    public Set<MemberOf> getMemberGroups() {
        return memberGroups;
    }

    public void addMemberGroup(MemberOf memberGroup) {
        this.memberGroups.add(memberGroup);
    }

    public Set<HasChildUser> getGroups() {
        return groups;
    }

    public void addGroup(HasChildUser hasChildUser) {
        this.groups.add(hasChildUser);
    }

    public Set<SharedWith> getSharedResources() {
        return sharedResources;
    }

    public void addSharedResource(SharedWith sharedResource) {
        this.sharedResources.add(sharedResource);
    }

    public Set<HasAccess> getAccessibleResources() {
        return accessibleResources;
    }

    public void addAccessibleResources(HasAccess accessibleResources) {
        this.accessibleResources.add(accessibleResources);
    }
}
