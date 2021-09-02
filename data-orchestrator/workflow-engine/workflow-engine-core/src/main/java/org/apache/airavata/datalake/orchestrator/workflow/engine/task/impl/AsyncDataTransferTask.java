package org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl;

import org.apache.airavata.datalake.orchestrator.workflow.engine.MFTCallbacSaveRequest;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowServiceGrpc;
import org.apache.airavata.datalake.orchestrator.workflow.engine.client.WorkflowEngineClient;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.BiSectionNonBlockingTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.NonBlockingTaskDef;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.TaskParam;
import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.MFTApiServiceGrpc;
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.apache.airavata.mft.api.service.TransferApiResponse;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.common.DelegateAuth;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonBlockingTaskDef(name = "AsyncDataTransferTask")
public class AsyncDataTransferTask extends BiSectionNonBlockingTask {

    private final static Logger logger = LoggerFactory.getLogger(AsyncDataTransferTask.class);

    @TaskParam(name = "SourceResourceId")
    private final ThreadLocal<String> sourceResourceId = new ThreadLocal<>();

    @TaskParam(name = "DestinationResourceId")
    private final ThreadLocal<String> destinationResourceId = new ThreadLocal<>();

    @TaskParam(name = "SourceCredToken")
    private final ThreadLocal<String> sourceCredToken = new ThreadLocal<>();

    @TaskParam(name = "DestinationCredToken")
    private final ThreadLocal<String> destinationCredToken = new ThreadLocal<>();

    @TaskParam(name = "CallbackUrl")
    private final ThreadLocal<String> callbackUrl = new ThreadLocal<>();

    @TaskParam(name = "MFTAPIHost")
    private final ThreadLocal<String> mftHost = new ThreadLocal<>();

    @TaskParam(name = "MFTAPIPort")
    private final ThreadLocal<Integer> mftPort = new ThreadLocal<>();

    // Security
    @TaskParam(name = "UserId")
    private final ThreadLocal<String> userId = new ThreadLocal<>();

    @TaskParam(name = "MFTClientId")
    private final ThreadLocal<String> mftClientId = new ThreadLocal<>();

    @TaskParam(name = "MFTClientSecret")
    private final ThreadLocal<String> mftClientSecret = new ThreadLocal<>();

    ///

    @TaskParam(name = "MFTCallbackStoreHost")
    private final ThreadLocal<String> mftCallbackStoreHost = new ThreadLocal<>();

    @TaskParam(name = "MFTCallbackStorePort")
    private final ThreadLocal<Integer> mftCallbackStorePort = new ThreadLocal<>();

    @TaskParam(name = "TenantId")
    private final ThreadLocal<String> tenantId = new ThreadLocal<>();


    public TaskResult beforeSection() {
        MFTApiServiceGrpc.MFTApiServiceBlockingStub mftClient = new MFTApiClient(getMftHost(), getMftPort()).get();
        TransferApiResponse submitResponse = mftClient.submitTransfer(TransferApiRequest.newBuilder()
                .setMftAuthorizationToken(AuthToken.newBuilder()
                        .setDelegateAuth(
                                DelegateAuth.newBuilder()
                                        .setUserId(getUserId())
                                        .setClientId(getMftClientId())
                                        .setClientSecret(getMftClientSecret()).build())
                        .build())
                .setSourceResourceId(getSourceResourceId())
                .setSourceToken(getSourceCredToken())
                .setDestinationResourceId(getDestinationResourceId())
                .setDestinationToken(getDestinationCredToken())
                .build());

        logger.info("Submitted transfer {} for task id {}", submitResponse.getTransferId(), getTaskId());

        WorkflowServiceGrpc.WorkflowServiceBlockingStub wfServiceClient = WorkflowEngineClient.buildClient(
                getMftCallbackStoreHost(), getMftCallbackStorePort());


        wfServiceClient.saveMFTCallback(MFTCallbacSaveRequest.newBuilder()
                .setMftTransferId(submitResponse.getTransferId())
                .setTaskId(getTaskId())
                .setWorkflowId(getCallbackContext().getJobConfig().getWorkflow())
                .setPrevSectionIndex(1).build());

        logger.info("Part 1 completed for task id {}", getTaskId());
        return new TaskResult(TaskResult.Status.COMPLETED, "Section 1 completed");
    }

    public TaskResult afterSection() {
        logger.info("Transfer completed successfully");
        // TODO update metadata into Datalake
        return new TaskResult(TaskResult.Status.COMPLETED, "Section 2 completed");
    }

    public String getSourceResourceId() {
        return sourceResourceId.get();
    }

    public void setSourceResourceId(String sourceResourceId) {
        this.sourceResourceId.set(sourceResourceId);
    }

    public String getDestinationResourceId() {
        return destinationResourceId.get();
    }

    public void setDestinationResourceId(String destinationResourceId) {
        this.destinationResourceId.set(destinationResourceId);
    }

    public String getSourceCredToken() {
        return sourceCredToken.get();
    }

    public void setSourceCredToken(String sourceCredToken) {
        this.sourceCredToken.set(sourceCredToken);
    }

    public String getDestinationCredToken() {
        return destinationCredToken.get();
    }

    public void setDestinationCredToken(String destinationCredToken) {
        this.destinationCredToken.set(destinationCredToken);
    }

    public String getUserId() {
        return userId.get();
    }

    public void setUserId(String userId) {
        this.userId.set(userId);
    }

    public String getCallbackUrl() {
        return callbackUrl.get();
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl.set(callbackUrl);
    }

    public String getMftHost() {
        return mftHost.get();
    }

    public void setMftHost(String mftHost) {
        this.mftHost.set(mftHost);
    }

    public Integer getMftPort() {
        return mftPort.get();
    }

    public void setMftPort(Integer mftPort) {
        this.mftPort.set(mftPort);
    }

    public String getMftClientId() {
        return mftClientId.get();
    }

    public void setMftClientId(String mftClientId) {
        this.mftClientId.set(mftClientId);
    }

    public String getMftClientSecret() {
        return mftClientSecret.get();
    }

    public void setMftClientSecret(String mftClientSecret) {
        this.mftClientSecret.set(mftClientSecret);
    }

    public String getMftCallbackStoreHost() {
        return mftCallbackStoreHost.get();
    }

    public void setMftCallbackStoreHost(String mftCallbackStoreHost) {
        this.mftCallbackStoreHost.set(mftCallbackStoreHost);
    }

    public Integer getMftCallbackStorePort() {
        return mftCallbackStorePort.get();
    }

    public void setMftCallbackStorePort(Integer mftCallbackStorePort) {
        this.mftCallbackStorePort.set(mftCallbackStorePort);
    }

    public String getTenantId() {
        return tenantId.get();
    }

    public void setTenantId(String tenantId) {
        this.tenantId.set(tenantId);
    }

}
