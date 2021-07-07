package org.apache.airavata.datalake.orchestrator.connectors;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.core.connector.AbstractConnector;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEntity;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEventRepository;
import org.apache.airavata.datalake.orchestrator.registry.persistance.EventStatus;
import org.apache.airavata.datalake.orchestrator.workflow.WorkflowServiceAuthToken;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowInvocationRequest;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowMessage;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drms connector to call DRMS services
 */

public class WorkflowServiceConnector implements AbstractConnector<Configuration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DRMSConnector.class);

    private ManagedChannel workflowChannel;
    private WorkflowServiceGrpc.WorkflowServiceBlockingStub workflowServiceStub;


    public WorkflowServiceConnector(Configuration configuration) throws Exception {
        this.init(configuration);
    }

    @Override
    public void init(Configuration configuration) throws Exception {
        this.workflowChannel = ManagedChannelBuilder
                .forAddress(configuration.getOutboundEventProcessor().getWorkflowEngineHost(),
                        configuration.getOutboundEventProcessor().getWorkflowPort()).usePlaintext().build();
        this.workflowServiceStub = WorkflowServiceGrpc.newBlockingStub(workflowChannel);
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    public void invokeWorkflow(DataOrchestratorEventRepository repository, DataOrchestratorEntity entity, GenericResource resource) {
        try {
            WorkflowServiceAuthToken workflowServiceAuthToken = WorkflowServiceAuthToken
                    .newBuilder()
                    .setAccessToken("")
                    .build();
            WorkflowMessage workflowMessage = WorkflowMessage.newBuilder()
                    .setResourceId(resource.getResourceId())
                    .build();

            WorkflowInvocationRequest workflowInvocationRequest = WorkflowInvocationRequest
                    .newBuilder().setMessage(workflowMessage).setAuthToken(workflowServiceAuthToken).build();
            this.workflowServiceStub.invokeWorkflow(workflowInvocationRequest);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while invoking workflow engine", entity.getResourceId(), ex);
            entity.setEventStatus(EventStatus.ERRORED.name());
            entity.setError("Error occurred while invoking workflow engine" + ex.getMessage());
            repository.save(entity);
            return;
        }
    }
}
