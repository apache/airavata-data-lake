package org.apache.airavata.drms.api.persistance.repository;

import org.apache.airavata.drms.api.persistance.model.ResourceProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResourcePropertyRepository extends JpaRepository<ResourceProperty, Long> {

    List<ResourceProperty> findByPropertyKeyAndResourceId(String key, String resourceId);

    List<ResourceProperty> findByPropertyKeyAndPropertyValueAndResourceId(String key,String value, String resourceId);

    List<ResourceProperty> findAllByResourceId(String resourceId);

    void deleteAllByPropertyKeyAndResourceId(String propertyKey, String resourceId);
}
