package org.apache.airavata.drms.api.persistance.repository;

import org.apache.airavata.drms.api.persistance.model.TransferMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferMappingRepository extends JpaRepository<TransferMapping, String> {


    Optional<TransferMapping> findTransferMappingBySourceIdAndDestinationId(String sourceId,
                                                                                            String destinationId);
    Optional<TransferMapping> findTransferMappingByScope(String scope);
}
