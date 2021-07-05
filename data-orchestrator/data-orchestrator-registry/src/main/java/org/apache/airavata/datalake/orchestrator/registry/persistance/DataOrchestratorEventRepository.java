package org.apache.airavata.datalake.orchestrator.registry.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DataOrchestratorEventRepository extends JpaRepository<DataOrchestratorEntity, String> {

    @Query(value = "select * from DATAORCHESTRATOR_ENTITY s where s.eventStatus = ?1  ORDER BY occurredTime DESC", nativeQuery = true)
    public List<DataOrchestratorEntity> findAllEntitiesWithGivenStatus(String eventStatus);


}
