package org.apache.airavata.drms.api.persistance.repository;

import org.apache.airavata.drms.api.persistance.model.ResourceProperty;
import org.apache.airavata.drms.api.persistance.model.TransferMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferMappingRepository extends JpaRepository<TransferMapping, String> {

}
