package org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes;

import org.apache.airavata.datalake.metadata.backend.neo4j.model.relationships.Has;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.relationships.HasRole;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NodeEntity
public class Tenant extends Entity {
    @Property(name = "tenant_id")
    private String tenantId;
    @Property(name = "domain")
    private String domain;
    @Property(name = "name")
    private String name;
    @Property(name = "requester_email")
    private String requesterEmail;
    @Property(name = "scope")
    private String scope;
    @Property(name = "redirect_uris")
    private List<String> redirectURIs;

    @Relationship(type = "HAS_ROLE")
    private Set<HasRole> roles = new HashSet<>();

    @Relationship(type = "HAS")
    private final Set<Has> entities = new HashSet<>();


    public Tenant() {
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getRedirectURIs() {
        return redirectURIs;
    }

    public void setRedirectURIs(List<String> redirectURIs) {
        this.redirectURIs = redirectURIs;
    }

    public Set<HasRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<HasRole> roles) {
        this.roles = roles;
    }


    public Set<Has> getEntities() {
        return entities;
    }

    public void addEntities(Has entity) {
        this.entities.add(entity);
    }

    @Override
    public String getSearchableId() {
        return this.getName()+"@"+this.getTenantId();
    }

    public void add(Entity entity, long relationShipCreatedAt, long relationShipModifiedAt,
                    Map<String, String> relationshipProperties) {

        Has has = new Has();
        has.setStartEntity(this);
        has.setEndEntity(entity);
        entity.setTenantInPointer(has);
        this.addEntities(has);

        if (relationShipCreatedAt != 0) {
            has.setCreatedAt(relationShipCreatedAt);
        }
        if (relationShipModifiedAt != 0) {
            has.setLastModifiedAt(relationShipModifiedAt);
        }
        if (relationshipProperties != null) {
            has.setProperties(relationshipProperties);
        }


    }
}
