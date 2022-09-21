package org.apache.airavata.drms.api.persistance.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "RESOURCE_PROPERTY")
@EntityListeners(AuditingEntityListener.class)
public class ResourceProperty {

    public ResourceProperty(String propertyKey, String propertyValue, Resource resource) {
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
        this.resource = resource;
    }

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "PROPERTY_KEY", nullable = false)
    private String propertyKey;

    @Column(name = "PROPERTY_VALUE", nullable = false)
    @Lob
    private String propertyValue;


    @ManyToOne
    @JoinColumn(name = "RESOURCE_ID")
    private Resource resource;

    public ResourceProperty() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String key) {
        this.propertyKey = key;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String value) {
        this.propertyValue = value;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceProperty that = (ResourceProperty) o;
        return Objects.equals(propertyKey, that.propertyKey) && Objects.equals(resource.getId(), that.resource.getId());
    }

    @Override
    public int hashCode() {
        if (propertyKey != null && resource != null) {
            return Objects.hash(propertyKey, resource.getId());
        } else {
            return Objects.hash(id);
        }
    }
}
