package org.apache.airavata.datalake.orchestrator.registry.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowEntityRepository extends JpaRepository<WorkflowEntity, String> {
}
