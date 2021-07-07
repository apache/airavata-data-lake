package org.apache.airavata.datalake.orchestrator.connectors;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.drms.AuthCredentialType;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.core.connector.AbstractConnector;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEntity;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEventRepository;
import org.apache.airavata.datalake.orchestrator.registry.persistance.EventStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DRMS connector to connect with DRMS service
 */
public class DRMSConnector implements AbstractConnector<Configuration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DRMSConnector.class);

    private ManagedChannel drmsChannel;
    private ResourceServiceGrpc.ResourceServiceBlockingStub resourceServiceBlockingStub;
    private StoragePreferenceServiceGrpc.StoragePreferenceServiceBlockingStub storagePreferenceServiceBlockingStub;

    public DRMSConnector(Configuration configuration) throws Exception {
        this.init(configuration);
    }

    @Override
    public void init(Configuration configuration) throws Exception {
        this.drmsChannel = ManagedChannelBuilder
                .forAddress(configuration.getOutboundEventProcessor().getDrmsHost(),
                        configuration.getOutboundEventProcessor().getDrmsPort()).usePlaintext().build();
        this.resourceServiceBlockingStub = ResourceServiceGrpc.newBlockingStub(drmsChannel);
        this.storagePreferenceServiceBlockingStub = StoragePreferenceServiceGrpc.newBlockingStub(drmsChannel);

    }

    @Override
    public void close() throws Exception {
        this.drmsChannel.shutdown();
    }

    @Override
    public boolean isOpen() {
        return !this.drmsChannel.isShutdown();
    }

    public Optional<String> getSourceStoragePreferenceId(DataOrchestratorEntity entity, String hostname) {
        DRMSServiceAuthToken serviceAuthToken = DRMSServiceAuthToken.newBuilder()
                .setAccessToken(entity.getAuthToken())
                .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                        .setUsername(entity.getOwnerId())
                        .setTenantId(entity.getTenantId())
                        .build())
                .build();
        FindTransferMappingsRequest request = FindTransferMappingsRequest.newBuilder()
                .setAuthToken(serviceAuthToken)
                .build();
        FindTransferMappingsResponse response = storagePreferenceServiceBlockingStub.getTransferMappings(request);
        List<TransferMapping> transferMappingList = response.getMappingsList();
        AtomicReference<String> storagePreferenceId = new AtomicReference<>(null);
        if (!transferMappingList.isEmpty()) {
            transferMappingList.forEach(transferMapping -> {
                if (transferMapping.getSourceStoragePreference().getStorageCase()
                        .equals(AnyStoragePreference.StorageCase.SSH_STORAGE_PREFERENCE)) {
                    if (transferMapping.getSourceStoragePreference().getSshStoragePreference()
                            .getStorage().getHostName().equals(hostname)) {
                        storagePreferenceId
                                .set(transferMapping.getSourceStoragePreference()
                                        .getSshStoragePreference().getStoragePreferenceId());
                    }
                }
            });
        }
        return Optional.ofNullable(storagePreferenceId.get());
    }

    public Optional<String> getDestinationStoragePreferenceId(DataOrchestratorEntity entity, String hostname) {
        DRMSServiceAuthToken serviceAuthToken = DRMSServiceAuthToken.newBuilder()
                .setAccessToken(entity.getAuthToken())
                .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                        .setUsername(entity.getOwnerId())
                        .setTenantId(entity.getTenantId())
                        .build())
                .build();
        FindTransferMappingsRequest request = FindTransferMappingsRequest.newBuilder()
                .setAuthToken(serviceAuthToken)
                .build();
        FindTransferMappingsResponse response = storagePreferenceServiceBlockingStub.getTransferMappings(request);
        List<TransferMapping> transferMappingList = response.getMappingsList();
        AtomicReference<String> storagePreferenceId = new AtomicReference<>(null);
        if (!transferMappingList.isEmpty()) {
            transferMappingList.forEach(transferMapping -> {
                if (transferMapping.getDestinationStoragePreference().getStorageCase()
                        .equals(AnyStoragePreference.StorageCase.SSH_STORAGE_PREFERENCE)) {
                    if (transferMapping.getDestinationStoragePreference().getSshStoragePreference()
                            .getStorage().getHostName().equals(hostname)) {
                        storagePreferenceId
                                .set(transferMapping.getDestinationStoragePreference()
                                        .getSshStoragePreference().getStoragePreferenceId());
                    }
                }
            });
        }
        return Optional.ofNullable(storagePreferenceId.get());
    }


    public Optional<GenericResource> createResource(DataOrchestratorEventRepository repository, DataOrchestratorEntity entity,
                                                    String resourceId,
                                                    String resourceName,
                                                    String resourcePath,
                                                    String parentId,
                                                    String type) {
        DRMSServiceAuthToken serviceAuthToken = DRMSServiceAuthToken.newBuilder()
                .setAccessToken(entity.getAuthToken())
                .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                        .setUsername(entity.getOwnerId())
                        .setTenantId(entity.getTenantId())
                        .build())
                .build();

        GenericResource genericResource = GenericResource
                .newBuilder()
                .setResourceId(resourceId)
                .setResourceName(resourceName)
                .setResourcePath(resourcePath)
                .setType(type)
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
            LOGGER.error("Error occurred while creating resource {} in DRMS", entity.getResourceId(), ex);
            entity.setEventStatus(EventStatus.ERRORED.name());
            entity.setError("Error occurred while creating resource in DRMS " + ex.getMessage());
            repository.save(entity);
            return Optional.empty();
        }
    }
}
