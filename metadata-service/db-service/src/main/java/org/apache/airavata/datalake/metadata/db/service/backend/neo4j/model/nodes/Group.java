package org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.nodes;

import org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.relationships.*;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NodeEntity
public class Group extends Entity {

    @Property(name = "name")
    private String name;

    @Property(name = "description")
    private String description;


    @Relationship(type = "HAS_CHILD_GROUP")
    private Set<HasChildGroup> childGroups = new HashSet<>();

    @Relationship(type = "HAS_CHILD_GROUP", direction = Relationship.INCOMING)
    private HasChildGroup ChildGroupInPointers;

    @Relationship(type = "HAS_CHILD_USER")
    private Set<HasChildUser> childUsers = new HashSet<>();

    @Relationship(type = "HAS_PARENT_GROUP", direction = Relationship.INCOMING)
    private Set<HasParentGroup> parentGroupInPointers = new HashSet<>();

    @Relationship(type = "HAS_PARENT_GROUP")
    private HasParentGroup parent;

    @Relationship(type = "MEMBER_OF", direction = Relationship.INCOMING)
    private Set<MemberOf> memberUsersInPointers = new HashSet<>();

    @Relationship(type = "SHARED_WITH", direction = Relationship.INCOMING)
    private Set<SharedWith> sharedResourcesInPointers = new HashSet<>();

    @Relationship(type = "HAS_ACCESS")
    private Set<HasAccess> accessibleResources = new HashSet<>();


    public Group() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Set<HasChildGroup> getChildGroups() {
        return childGroups;
    }

    public void addChildGroup(HasChildGroup childGroup) {
        this.childGroups.add(childGroup);
    }

    public Set<HasChildUser> getChildUsers() {
        return childUsers;
    }

    public void addChildUser(HasChildUser childUser) {
        this.childUsers.add(childUser);
    }


    public Set<MemberOf> getMemberUsersInPointers() {
        return memberUsersInPointers;
    }

    private void addMemberUserInPointer(MemberOf memberUser) {
        this.memberUsersInPointers.add(memberUser);
    }

    public Set<SharedWith> getSharedResourcesInPointers() {
        return sharedResourcesInPointers;
    }

    public void addSharedResourcesInPointers(SharedWith sharedResourcesInPointer) {
        this.sharedResourcesInPointers.add(sharedResourcesInPointer);
    }

    public Set<HasAccess> getAccessibleResources() {
        return accessibleResources;
    }

    public void addAccessibleResources(HasAccess accessibleResources) {
        this.accessibleResources.add(accessibleResources);
    }

    public void setChildGroups(Set<HasChildGroup> childGroups) {
        this.childGroups = childGroups;
    }

    public HasChildGroup getChildGroupInPointers() {
        return ChildGroupInPointers;
    }

    public void setChildGroupInPointers(HasChildGroup childGroupInPointers) {
        this.ChildGroupInPointers = childGroupInPointers;
    }

    public void setChildUsers(Set<HasChildUser> childUsers) {
        this.childUsers = childUsers;
    }

    public Set<HasParentGroup> getParentGroupInPointers() {
        return parentGroupInPointers;
    }

    private void addParentGroupInPointer(HasParentGroup parentGroup) {
        this.parentGroupInPointers.add(parentGroup);
    }

    public HasParentGroup getParent() {
        return parent;
    }

    public void setParent(HasParentGroup parent) {
        this.parent = parent;
    }

    public void setMemberUsersInPointers(Set<MemberOf> memberUsersInPointers) {
        this.memberUsersInPointers = memberUsersInPointers;
    }

    public void addChildUser(User user, String membership, long relationshipCreatedAt,
                             long relationshipModifiedAt, Map<String, String> relationshipProperties) {
        MemberOf memberOf = new MemberOf();
        memberOf.setMembershipType(membership);
        memberOf.setStartEntity(user);
        memberOf.setEndEntity(this);
        user.addMemberGroup(memberOf);
        this.addMemberUserInPointer(memberOf);

        HasChildUser childUser = new HasChildUser();
        childUser.setUserType(membership);
        childUser.setStartEntity(this);
        childUser.setEndEntity(user);
        user.addGroup(childUser);
        this.addChildUser(childUser);

        if (relationshipCreatedAt != 0) {
            memberOf.setCreatedAt(relationshipCreatedAt);
            childUser.setCreatedAt(relationshipCreatedAt);
        }
        if (relationshipModifiedAt != 0) {
            memberOf.setLastModifiedAt(relationshipModifiedAt);
            childUser.setLastModifiedAt(relationshipModifiedAt);
        }
        if (relationshipProperties != null) {
            memberOf.setProperties(relationshipProperties);
            childUser.setProperties(relationshipProperties);
        }

    }


    public void addChildGroup(Group group, long relationShipCreatedAt,
                              long relationShipModifiedAt, Map<String, String> relationshipProperties) {

        HasChildGroup hasChildGroup = new HasChildGroup();
        hasChildGroup.setStartEntity(this);
        hasChildGroup.setEndEntity(group);
        this.addChildGroup(hasChildGroup);
        group.setChildGroupInPointers(hasChildGroup);

        HasParentGroup hasParentGroup = new HasParentGroup();
        hasParentGroup.setStartEntity(group);
        hasParentGroup.setEndEntity(this);
        group.setParent(hasParentGroup);
        this.addParentGroupInPointer(hasParentGroup);

        if (relationShipCreatedAt != 0) {
            hasParentGroup.setCreatedAt(relationShipCreatedAt);
            hasChildGroup.setCreatedAt(relationShipCreatedAt);
        }
        if (relationShipModifiedAt != 0) {
            hasParentGroup.setLastModifiedAt(relationShipModifiedAt);
            hasChildGroup.setLastModifiedAt(relationShipModifiedAt);
        }
        if (relationshipProperties != null) {
            hasParentGroup.setProperties(relationshipProperties);
            hasChildGroup.setProperties(relationshipProperties);
        }

    }


}
