package org.apache.airavata.drms.api.persistance.repository;

import org.apache.airavata.drms.api.persistance.model.ResourceProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResourcePropertyRepository extends JpaRepository<ResourceProperty, String> {

    Optional<ResourceProperty> findByPropertyKeyAndResourceId(String key, String resourceId);
}
