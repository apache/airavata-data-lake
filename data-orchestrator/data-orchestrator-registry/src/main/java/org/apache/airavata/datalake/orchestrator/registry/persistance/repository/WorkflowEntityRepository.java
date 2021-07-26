package org.apache.airavata.datalake.orchestrator.registry.persistance.repository;

import org.apache.airavata.datalake.orchestrator.registry.persistance.entity.WorkflowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowEntityRepository extends JpaRepository<WorkflowEntity, String> {
}
