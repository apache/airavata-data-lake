package org.apache.airavata.datalake.orchestrator.workflow.engine.services.handler;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowInvocationRequest;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowInvocationResponse;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowServiceGrpc;
import org.apache.airavata.datalake.orchestrator.workflow.engine.services.wm.PreWorkflowManager;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class WorkflowEngineAPIHandler extends WorkflowServiceGrpc.WorkflowServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowEngineAPIHandler.class);

    private PreWorkflowManager preworkflowManager;


    @Autowired
    public WorkflowEngineAPIHandler(PreWorkflowManager preworkflowManager) {
        this.preworkflowManager = preworkflowManager;
    }

    @Override
    public void invokeWorkflow(WorkflowInvocationRequest request,
                               StreamObserver<WorkflowInvocationResponse> responseObserver) {
        try {
            preworkflowManager.launchWorkflow(request.getMessage().getMessageId());


        } catch (Exception ex) {
            String msg = "Error occurred while invoking blocking pipeline";
            LOGGER.error(msg, ex);
            throw new RuntimeException(msg, ex);
        }
    }
}
