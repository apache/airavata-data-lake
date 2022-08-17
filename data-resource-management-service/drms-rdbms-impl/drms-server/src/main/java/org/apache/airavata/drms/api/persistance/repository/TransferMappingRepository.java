package org.apache.airavata.drms.api.persistance.repository;

import org.apache.airavata.drms.api.persistance.model.TransferMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferMappingRepository extends JpaRepository<TransferMapping, String> {


    Optional<TransferMapping> findTransferMappingBySourceResourceIdAndDestinationResourceId(String sourceId,
                                                                                            String destinationId);
}
