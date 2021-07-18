package org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.drms.AuthCredentialType;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.storage.ResourceFetchRequest;
import org.apache.airavata.datalake.drms.storage.ResourceFetchResponse;
import org.apache.airavata.datalake.drms.storage.ResourceServiceGrpc;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.NonBlockingTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.NonBlockingSection;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.NonBlockingTaskDef;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.TaskParam;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.resource.secret.management.client.ResourceSecretManagementClient;
import org.apache.custos.resource.secret.service.SSHCredential;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonBlockingTaskDef(name = "DataTransferPreValidationTask")
public class DataTransferPreValidationTask extends NonBlockingTask {

    private final static Logger logger = LoggerFactory.getLogger(DataTransferPreValidationTask.class);


    @TaskParam(name = "SourceResourceId")
    private final ThreadLocal<String> sourceResourceId = new ThreadLocal<>();

    @TaskParam(name = "DestinationResourceId")
    private final ThreadLocal<String> destinationResourceId = new ThreadLocal<>();

    @TaskParam(name = "SourceCredToken")
    private final ThreadLocal<String> sourceCredToken = new ThreadLocal<>();

    @TaskParam(name = "DestinationCredToken")
    private final ThreadLocal<String> destinationCredToken = new ThreadLocal<>();

    @TaskParam(name = "DRMSHost")
    private final ThreadLocal<String> drmsHost = new ThreadLocal<>();

    @TaskParam(name = "DRMSPort")
    private final ThreadLocal<Integer> drmsPort = new ThreadLocal<>();


    @TaskParam(name = "CustosHost")
    private final ThreadLocal<String> custosHost = new ThreadLocal<>();

    @TaskParam(name = "CustosPort")
    private final ThreadLocal<Integer> custosPort = new ThreadLocal<>();

    @TaskParam(name = "CustosId")
    private final ThreadLocal<String> custosId = new ThreadLocal<>();

    @TaskParam(name = "CustosSecret")
    private final ThreadLocal<String> custosSecret = new ThreadLocal<>();


    // Security
    @TaskParam(name = "UserId")
    private final ThreadLocal<String> userId = new ThreadLocal<>();


    @TaskParam(name = "TenantId")
    private final ThreadLocal<String> tenantId = new ThreadLocal<>();


    @TaskParam(name = "AuthToken")
    private final ThreadLocal<String> authToken = new ThreadLocal<>();


    @NonBlockingSection(sectionIndex = 1)
    public TaskResult section1() {

        try {
            CustosClientProvider custosClientProvider = new CustosClientProvider
                    .Builder()
                    .setServerHost(custosHost.get())
                    .setServerPort(custosPort.get())
                    .setClientSec(custosSecret.get())
                    .setClientId(custosId.get())
                    .build();

            ResourceSecretManagementClient resourceSecretManagementClient = custosClientProvider
                    .getResourceSecretManagementClient();

            SSHCredential sshSourceCredential = resourceSecretManagementClient.getSSHCredential(custosId.get(),
                    sourceCredToken.get(), false);
            SSHCredential sshDstCredential = resourceSecretManagementClient.getSSHCredential(custosId.get(),
                    destinationCredToken.get(), false);
            if (sshSourceCredential.hasMetadata() && sshDstCredential.hasMetadata()) {
                logger.info("Successfully validated credential tokens ...");
                return new TaskResult(TaskResult.Status.COMPLETED, "Completed");
            }

        } catch (Exception ex) {
            String msg = "Error occurred while validating credential tokens Reason: " + ex.getMessage();
            logger.error(msg, ex);
            return new TaskResult(TaskResult.Status.FAILED, msg);
        }
        return new TaskResult(TaskResult.Status.FAILED, "Credential token validation failed");
    }

    @NonBlockingSection(sectionIndex = 2)
    public TaskResult section2() {
        try {
            logger.info("Running section 2......");
            DRMSServiceAuthToken serviceAuthToken = DRMSServiceAuthToken.newBuilder()
                    .setAccessToken(authToken.get())
                    .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                    .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                            .setUsername(userId.get())
                            .setTenantId(tenantId.get())
                            .build())
                    .build();


            //TODO: add ssl
            ManagedChannel drmsChannel = ManagedChannelBuilder
                    .forAddress(drmsHost.get(),
                            drmsPort.get()).usePlaintext().build();

            ResourceServiceGrpc.ResourceServiceBlockingStub resourceServiceBlockingStub =
                    ResourceServiceGrpc.newBlockingStub(drmsChannel);

            ResourceFetchRequest sourceResourceFetchRequest = ResourceFetchRequest
                    .newBuilder()
                    .setResourceId(sourceResourceId.get())
                    .setAuthToken(serviceAuthToken)
                    .build();

            ResourceFetchRequest dstResourceFetchRequest = ResourceFetchRequest
                    .newBuilder()
                    .setResourceId(destinationResourceId.get())
                    .setAuthToken(serviceAuthToken)
                    .build();

            ResourceFetchResponse srcResponse = resourceServiceBlockingStub
                    .fetchResource(sourceResourceFetchRequest);

            ResourceFetchResponse dstResponse = resourceServiceBlockingStub
                    .fetchResource(dstResourceFetchRequest);

            if (sourceResourceId.get().equals(srcResponse.getResource().getResourceId())
                    && destinationResourceId.get().equals(dstResponse.getResource().getResourceId())) {
                logger.info("Successfully validated resource access  ...");
                return new TaskResult(TaskResult.Status.COMPLETED, "Validation completeed");
            } else {
                logger.error("Validation failed ....");
                logger.info(srcResponse.getResource().getResourceId());
                logger.info(dstResponse.getResource().getResourceId());
            }

        } catch (Exception exception) {
            String msg = "Error occurred while validating resource access : " + exception.getMessage();
            logger.error(msg, exception);
            return new TaskResult(TaskResult.Status.FAILED, msg);
        }
        return new TaskResult(TaskResult.Status.FAILED, "Error occurred while validating resource access");
    }


    public String getSourceResourceId() {
        return sourceResourceId.get();
    }

    public String getDestinationResourceId() {
        return destinationResourceId.get();
    }

    public String getSourceCredToken() {
        return sourceCredToken.get();
    }

    public String getDestinationCredToken() {
        return destinationCredToken.get();
    }

    public String getDrmsHost() {
        return drmsHost.get();
    }

    public Integer getDrmsPort() {
        return drmsPort.get();
    }

    public String getCustosHost() {
        return custosHost.get();
    }

    public Integer getCustosPort() {
        return custosPort.get();
    }

    public String getUserId() {
        return userId.get();
    }

    public String getTenantId() {
        return tenantId.get();
    }

    public void setSourceResourceId(String sourceId) {
        sourceResourceId.set(sourceId);
    }

    public void setDestinationResourceId(String destinationId) {
        destinationResourceId.set(destinationId);
    }

    public void setSourceCredToken(String srcCredToken) {
        sourceCredToken.set(srcCredToken);
    }

    public void setDestinationCredToken(String dstCredToken) {
        destinationCredToken.set(dstCredToken);
    }

    public void setDrmsHost(String drHost) {
        drmsHost.set(drHost);
    }

    public void setDrmsPort(Integer port) {
        drmsPort.set(port);
    }

    public void setCustosHost(String host) {
        custosHost.set(host);
    }

    public void setCustosPort(Integer port) {
        custosPort.set(port);
    }

    public void setUserId(String usrId) {
        userId.set(usrId);
    }

    public void setTenantId(String tntId) {
        tenantId.set(tntId);
    }

    public String getAuthToken() {
        return authToken.get();
    }

    public void setAuthToken(String accessToken) {
        authToken.set(accessToken);
    }


    public String getCustosId() {
        return custosId.get();
    }

    public void setCustosId(String cusId) {
        custosId.set(cusId);
    }

    public void setCustosSecret(String cusSecret) {
        custosSecret.set(cusSecret);
    }

    public String getCustosSecret() {
        return custosSecret.get();
    }
}
