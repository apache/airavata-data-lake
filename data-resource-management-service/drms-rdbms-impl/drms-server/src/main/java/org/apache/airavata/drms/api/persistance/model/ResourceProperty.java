package org.apache.airavata.drms.api.persistance.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "resource_property",uniqueConstraints={
        @UniqueConstraint( name = "idx_key_vaule",  columnNames ={"key","vaule","resource_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class ResourceProperty {

    public ResourceProperty(String key, String value, Resource resource) {
        this.key = key;
        this.value = value;
        this.resource = resource;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    @Lob
    private String value;


    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Resource resource;

    public ResourceProperty() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

}
