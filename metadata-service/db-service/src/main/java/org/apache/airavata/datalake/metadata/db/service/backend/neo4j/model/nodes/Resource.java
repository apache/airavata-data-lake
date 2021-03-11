package org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.nodes;

import org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.relationships.*;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NodeEntity
public class Resource extends Entity {

    @Id
    @GeneratedValue
    private Long id;

    @Property(name = "name")
    private String name;

    @Relationship(type = "HAS", direction = Relationship.INCOMING)
    private Has tenantPointer;

    @Relationship(type = "HAS_CHILD_RESOURCE")
    private Set<HasChildResource> childResourceSet = new HashSet<>();

    @Relationship(type = "HAS_CHILD_RESOURCE", direction = Relationship.INCOMING)
    private HasChildResource parentInPointer;

    @Relationship(type = "HAS_PARENT_RESOURCE", direction = Relationship.INCOMING)
    private Set<HasParentResource> childInPointers = new HashSet<>();

    @Relationship(type = "HAS_PARENT_RESOURCE")
    private HasParentResource parent;

    @Relationship(type = "SHARED_WITH")
    private Set<SharedWith> shares = new HashSet<>();

    @Relationship(type = "HAS_ACCESS", direction = Relationship.INCOMING)
    private Set<HasAccess> accesses = new HashSet<>();

    public Resource() {
    }


    public Has getTenantPointer() {
        return tenantPointer;
    }

    public void setTenantPointer(Has tenantPointer) {
        this.tenantPointer = tenantPointer;
    }

    public Set<HasChildResource> getChildResourceSet() {
        return childResourceSet;
    }

    public void addChildResource(HasChildResource childResource) {
        this.childResourceSet.add(childResource);
    }

    public Set<HasParentResource> getChildInPointers() {
        return childInPointers;
    }

    public void addChildInPointers(HasParentResource childInPointers) {
        this.childInPointers.add(childInPointers);
    }

    public HasParentResource getParent() {
        return parent;
    }

    public void setParent(HasParentResource parent) {
        this.parent = parent;
    }

    public HasChildResource getParentInPointer() {
        return parentInPointer;
    }

    public void setParentInPointer(HasChildResource parentInPointer) {
        this.parentInPointer = parentInPointer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SharedWith> getShares() {
        return shares;
    }

    public void addShares(SharedWith share) {
        this.shares.add(share);
    }

    public Set<HasAccess> getAccesses() {
        return accesses;
    }

    public void addAccess(HasAccess access) {
        this.accesses.add(access);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void addChildResource(Resource resource, long createdAt, long lastModifiedAt,
                                 Map<String, String> properties) {
        HasChildResource hasChildResource = new HasChildResource();
        hasChildResource.setStartEntity(this);
        hasChildResource.setEndEntity(resource);
        this.addChildResource(hasChildResource);
        resource.setParentInPointer(hasChildResource);

        HasParentResource hasParentResource = new HasParentResource();
        hasParentResource.setStartEntity(resource);
        hasParentResource.setEndEntity(this);
        resource.setParent(hasParentResource);
        this.addChildInPointers(hasParentResource);

        if (createdAt != 0) {
            hasParentResource.setCreatedAt(createdAt);
            hasChildResource.setCreatedAt(createdAt);
        }
        if (lastModifiedAt != 0) {
            hasParentResource.setLastModifiedAt(lastModifiedAt);
            hasChildResource.setLastModifiedAt(lastModifiedAt);
        }
        if (properties != null) {
            hasParentResource.setProperties(properties);
            hasChildResource.setProperties(properties);
        }

    }

    public void shareWithAUser(User user, String permission, long createdAt, long lastModifiedAt,
                               Map<String, String> properties) {

        SharedWith sharedWith = new SharedWith();
        sharedWith.setStartEntity(this);
        sharedWith.setEndEntity(user);
        sharedWith.setPermissionType(permission);
        this.addShares(sharedWith);
        user.addSharedResource(sharedWith);

        HasAccess hasAccess = new HasAccess();
        hasAccess.setStartEntity(user);
        hasAccess.setEndEntity(this);
        hasAccess.setPermissionType(permission);
        this.addAccess(hasAccess);
        user.addAccessibleResources(hasAccess);

        if (createdAt != 0) {
            sharedWith.setCreatedAt(createdAt);
            hasAccess.setCreatedAt(createdAt);
        }
        if (lastModifiedAt != 0) {
            sharedWith.setLastModifiedAt(lastModifiedAt);
            hasAccess.setLastModifiedAt(lastModifiedAt);
        }
        if (properties != null) {
            sharedWith.setProperties(properties);
            hasAccess.setProperties(properties);
        }

    }

    public void shareWithAGroup(Group group, String permission, long relationshipCreatedAt, long relationshipLastModifiedAt,
                                Map<String, String> relationshipProperties) {

        SharedWith sharedWith = new SharedWith();
        sharedWith.setStartEntity(this);
        sharedWith.setEndEntity(group);
        sharedWith.setPermissionType(permission);
        this.addShares(sharedWith);
        group.addSharedResourcesInPointers(sharedWith);

        HasAccess hasAccess = new HasAccess();
        hasAccess.setStartEntity(group);
        hasAccess.setEndEntity(this);
        hasAccess.setPermissionType(permission);
        this.addAccess(hasAccess);
        group.addAccessibleResources(hasAccess);

        if (relationshipCreatedAt != 0) {
            sharedWith.setCreatedAt(relationshipCreatedAt);
            hasAccess.setCreatedAt(relationshipCreatedAt);
        }
        if (relationshipLastModifiedAt != 0) {
            sharedWith.setLastModifiedAt(relationshipLastModifiedAt);
            hasAccess.setLastModifiedAt(relationshipLastModifiedAt);
        }
        if (relationshipProperties != null) {
            sharedWith.setProperties(relationshipProperties);
            hasAccess.setProperties(relationshipProperties);
        }


    }


}
