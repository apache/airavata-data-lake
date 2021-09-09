package org.apache.airavata.datalake.orchestrator.connectors;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.drms.AuthCredentialType;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.drms.sharing.DRMSSharingServiceGrpc;
import org.apache.airavata.datalake.drms.sharing.ShareEntityWithGroupRequest;
import org.apache.airavata.datalake.drms.sharing.ShareEntityWithUserRequest;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.core.connector.AbstractConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DRMS connector to connect with DRMS service
 */
public class DRMSConnector implements AbstractConnector<Configuration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DRMSConnector.class);

    private ManagedChannel drmsChannel;
    private ResourceServiceGrpc.ResourceServiceBlockingStub resourceServiceBlockingStub;
    private StorageServiceGrpc.StorageServiceBlockingStub storageServiceBlockingStub;
    private StoragePreferenceServiceGrpc.StoragePreferenceServiceBlockingStub storagePreferenceServiceBlockingStub;
    private DRMSSharingServiceGrpc.DRMSSharingServiceBlockingStub sharingServiceBlockingStub;

    public DRMSConnector(Configuration configuration) throws Exception {
        this.init(configuration);
    }

    @Override
    public void init(Configuration configuration) throws Exception {
        this.drmsChannel = ManagedChannelBuilder
                .forAddress(configuration.getOutboundEventProcessor().getDrmsHost(),
                        configuration.getOutboundEventProcessor().getDrmsPort()).usePlaintext().build();
        this.resourceServiceBlockingStub = ResourceServiceGrpc.newBlockingStub(drmsChannel);
        this.storageServiceBlockingStub = StorageServiceGrpc.newBlockingStub(drmsChannel);
        this.storagePreferenceServiceBlockingStub = StoragePreferenceServiceGrpc.newBlockingStub(drmsChannel);
        this.sharingServiceBlockingStub = DRMSSharingServiceGrpc.newBlockingStub(drmsChannel);
    }

    @Override
    public void close() throws Exception {
        this.drmsChannel.shutdown();
    }

    @Override
    public boolean isOpen() {
        return !this.drmsChannel.isShutdown();
    }

    public void shareWithUser(String authToken, String tenantId, String admin, String user, String resourceId, String permission) throws Exception {

        DRMSServiceAuthToken serviceAuthToken = DRMSServiceAuthToken.newBuilder()
                .setAccessToken(authToken)
                .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                        .setUsername(admin)
                        .setTenantId(tenantId)
                        .build())
                .build();

        ShareEntityWithUserRequest.Builder shareBuilder = ShareEntityWithUserRequest.newBuilder()
                .setAuthToken(serviceAuthToken)
                .setEntityId(resourceId)
                .setSharedUserId(user)
                .setPermissionId(permission);

        this.sharingServiceBlockingStub.shareEntityWithUser(shareBuilder.build());

    }


    public void shareWithGroup(String authToken, String tenantId, String admin, String groupId, String resourceId,
                               String permission) throws Exception {

        DRMSServiceAuthToken serviceAuthToken = DRMSServiceAuthToken.newBuilder()
                .setAccessToken(authToken)
                .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                        .setUsername(admin)
                        .setTenantId(tenantId)
                        .build())
                .build();

        ShareEntityWithGroupRequest.Builder shareBuilder = ShareEntityWithGroupRequest.newBuilder()
                .setAuthToken(serviceAuthToken)
                .setEntityId(resourceId)
                .setSharedGroupId(groupId)
                .setPermissionId(permission);

        this.sharingServiceBlockingStub.shareEntityWithGroup(shareBuilder.build());

    }

    public Optional<TransferMapping> getActiveTransferMapping(String authToken, String tenantId,
                                                              String user, String hostName) throws Exception {

        DRMSServiceAuthToken serviceAuthToken = DRMSServiceAuthToken.newBuilder()
                .setAccessToken(authToken)
                .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                        .setUsername(user)
                        .setTenantId(tenantId)
                        .build())
                .build();
        FindTransferMappingsRequest request = FindTransferMappingsRequest.newBuilder()
                .setAuthToken(serviceAuthToken)
                .build();
        FindTransferMappingsResponse response = storageServiceBlockingStub.getTransferMappings(request);
        List<TransferMapping> transferMappingList = response.getMappingsList();
        AtomicReference<TransferMapping> transferMappingOp = new AtomicReference<>(null);
        if (!transferMappingList.isEmpty()) {
            transferMappingList.forEach(transferMapping -> {
                if (transferMapping.getSourceStorage().getStorageCase()
                        .equals(AnyStorage.StorageCase.SSH_STORAGE)) {
                    if (transferMapping.getSourceStorage().getSshStorage().getHostName().equals(hostName)) {
                        transferMappingOp.set(transferMapping);
                    }
                }
            });
        }
        return Optional.ofNullable(transferMappingOp.get());
    }


    public Optional<GenericResource> createResource(String authToken,
                                                    String tenantId,
                                                    String resourceId,
                                                    String resourceName,
                                                    String resourcePath,
                                                    String parentId,
                                                    String type, String parentType,
                                                    String user) throws Exception {


        DRMSServiceAuthToken serviceAuthToken = DRMSServiceAuthToken.newBuilder()
                .setAccessToken(authToken)
                .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                        .setUsername(user)
                        .setTenantId(tenantId)
                        .build())
                .build();

        GenericResource genericResource = GenericResource
                .newBuilder()
                .setResourceId(resourceId)
                .setResourceName(resourceName)
                .setResourcePath(resourcePath)
                .setType(type)
                .putProperties("PARENT_TYPE", parentType)
                .setParentId(parentId).build();
        ResourceCreateRequest resourceCreateRequest = ResourceCreateRequest
                .newBuilder()
                .setAuthToken(serviceAuthToken)
                .setResource(genericResource)
                .build();

        try {
            ResourceCreateResponse resourceCreateResponse = resourceServiceBlockingStub.createResource(resourceCreateRequest);
            return Optional.ofNullable(resourceCreateResponse.getResource());
        } catch (Exception ex) {
            LOGGER.error("Error occurred while creating resource {} in DRMS", resourcePath, ex);
            return Optional.empty();
        }
    }

    public void addResourceMetadata(String authToken,
                                    String tenantId,
                                    String resourceId,
                                    String user,
                                    String type,
                                    Map<String, String> metadata) {

        DRMSServiceAuthToken serviceAuthToken = DRMSServiceAuthToken.newBuilder()
                .setAccessToken(authToken)
                .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                        .setUsername(user)
                        .setTenantId(tenantId)
                        .build())
                .build();

        Struct.Builder structBuilder = Struct.newBuilder();
        metadata.forEach((key, value) -> structBuilder.putFields(key,
                Value.newBuilder().setStringValue(value).build()));

        resourceServiceBlockingStub.addResourceMetadata(AddResourceMetadataRequest.newBuilder()
                .setResourceId(resourceId)
                .setAuthToken(serviceAuthToken)
                .setType(type)
                .setMetadata(structBuilder.build()).build());
    }


    public Optional<AnyStoragePreference> getStoragePreference(String authToken, String username, String tenantId, String storageId) {
        DRMSServiceAuthToken serviceAuthToken = DRMSServiceAuthToken.newBuilder()
                .setAccessToken(authToken)
                .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                        .setUsername(username)
                        .setTenantId(tenantId)
                        .build())
                .build();
        StoragePreferenceSearchQuery searchQuery = StoragePreferenceSearchQuery
                .newBuilder()
                .setField("storageId")
                .setValue(storageId)
                .build();

        StoragePreferenceSearchRequest storagePreferenceSearchRequest = StoragePreferenceSearchRequest
                .newBuilder()
                .setAuthToken(serviceAuthToken)
                .addQueries(searchQuery)
                .build();
        StoragePreferenceSearchResponse response = storagePreferenceServiceBlockingStub
                .searchStoragePreference(storagePreferenceSearchRequest);
        List<AnyStoragePreference> preferences = response.getStoragesPreferenceList();
        if (!preferences.isEmpty()) {
            return Optional.ofNullable(preferences.get(0));
        }
        return Optional.empty();
    }

}
