package org.apache.airavata.datalake.orchestrator.workflow.engine.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowInvocationRequest;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowMessage;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowServiceGrpc;

public class WorkflowEngineClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();
        WorkflowServiceGrpc.WorkflowServiceBlockingStub workflowServiceStub =
                WorkflowServiceGrpc.newBlockingStub(channel);
        WorkflowInvocationRequest workflowInvocationRequest = WorkflowInvocationRequest
                .newBuilder()
                .setMessage(WorkflowMessage.newBuilder().setMessageId("387bd8e9-58ce-4626-8347-90a8ab12376e "))
                .build();
        workflowServiceStub.invokeWorkflow(workflowInvocationRequest);
    }
}
