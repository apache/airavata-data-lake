package org.apache.airavata.datalake.orchestrator.workflow.engine.services.wm;

import org.apache.airavata.datalake.orchestrator.registry.persistance.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * A class responsible to create task DAG and launch experiment
 */
@Component
public class PreWorkflowManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreWorkflowManager.class);

    private DataOrchestratorEventRepository dataOrchestratorEventRepository;

    private WorkflowEntityRepository workflowEntityRepository;

    @Autowired
    public PreWorkflowManager(DataOrchestratorEventRepository dataOrchestratorEventRepository,
                              WorkflowEntityRepository workflowEntityRepository) {
        this.dataOrchestratorEventRepository = dataOrchestratorEventRepository;
        this.workflowEntityRepository = workflowEntityRepository;
    }

    public boolean launchWorkflow(String msgId) {

        Optional<DataOrchestratorEntity> dataOrchestratorEntity = dataOrchestratorEventRepository.findById(msgId);
        dataOrchestratorEntity.ifPresent(enty -> {
            String workflowId = "WORKFLOW_" + UUID.randomUUID().toString();
            WorkflowEntity workFlowEntity = new WorkflowEntity();
            workFlowEntity.setId(workflowId);
            workFlowEntity.setDataOrchestratorEntity(enty);
            workFlowEntity.setStatus(EntityStatus.WORKFLOW_LAUNCHED.name());
            workflowEntityRepository.save(workFlowEntity);
        });

        return true;
    }


}
