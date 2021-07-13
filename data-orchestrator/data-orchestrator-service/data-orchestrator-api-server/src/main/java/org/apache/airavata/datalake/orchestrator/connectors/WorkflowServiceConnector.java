package org.apache.airavata.datalake.orchestrator.connectors;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.core.connector.AbstractConnector;
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

    public void invokeWorkflow(String username, String tenantId, String sourceResourceId, String sourceCredentialToken,
                               String dstResourceId, String destinationCredentialToken) {
        try {
            WorkflowMessage workflowMessage = WorkflowMessage.newBuilder()
                    .setSourceResourceId(sourceResourceId)
                    .setDestinationResourceId(dstResourceId)
                    .setUsername(username)
                    .setTenantId(tenantId)
                    .setSourceCredentialToken(sourceCredentialToken)
                    .setDestinationCredentialToken(destinationCredentialToken)
                    .build();
            WorkflowInvocationRequest workflowInvocationRequest = WorkflowInvocationRequest
                    .newBuilder().setMessage(workflowMessage).build();
            this.workflowServiceStub.invokeWorkflow(workflowInvocationRequest);
        } catch (Exception ex) {
            String msg = "Error occurred while invoking workflow engine " + ex.getMessage();
            throw new RuntimeException(msg, ex);
        }
    }
}
