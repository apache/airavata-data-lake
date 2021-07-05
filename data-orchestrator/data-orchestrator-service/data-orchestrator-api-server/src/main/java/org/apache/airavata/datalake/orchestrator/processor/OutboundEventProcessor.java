package org.apache.airavata.datalake.orchestrator.processor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.drms.AuthCredentialType;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.core.processor.MessageProcessor;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEntity;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEventRepository;
import org.apache.airavata.datalake.orchestrator.registry.persistance.EventStatus;
import org.apache.airavata.datalake.orchestrator.workflow.WorkflowServiceAuthToken;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowInvocationRequest;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowMessage;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowServiceGrpc;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;
import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible  and publish events to registry and
 * Workflow engine
 */
public class OutboundEventProcessor implements MessageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutboundEventProcessor.class);

    private DozerBeanMapper dozerBeanMapper;
    private DataOrchestratorEventRepository repository;

    private final ManagedChannel workflowChannel;
    private final ManagedChannel drmsChannel;
    private final WorkflowServiceGrpc.WorkflowServiceBlockingStub workflowServiceStub;
    private final ResourceServiceGrpc.ResourceServiceBlockingStub resourceServiceBlockingStub;
    private final StoragePreferenceServiceGrpc.StoragePreferenceServiceBlockingStub storagePreferenceServiceBlockingStub;

    public OutboundEventProcessor(Configuration configuration, DataOrchestratorEventRepository repository) throws Exception {
        this.repository = repository;
        this.workflowChannel = ManagedChannelBuilder
                .forAddress(configuration.getOutboundEventProcessor().getWorkflowEngineHost(),
                        configuration.getOutboundEventProcessor().getWorkflowPort()).usePlaintext().build();
        this.drmsChannel = ManagedChannelBuilder.forAddress(configuration.getOutboundEventProcessor().getDrmsHost(),
                configuration.getOutboundEventProcessor().getDrmsPort()).usePlaintext().build();
        this.workflowServiceStub = WorkflowServiceGrpc.newBlockingStub(workflowChannel);
        this.resourceServiceBlockingStub = ResourceServiceGrpc.newBlockingStub(drmsChannel);
        this.storagePreferenceServiceBlockingStub = StoragePreferenceServiceGrpc.newBlockingStub(drmsChannel);
        this.init();
    }

    @Override
    public void init() throws Exception {
        dozerBeanMapper = new DozerBeanMapper();
        BeanMappingBuilder orchestratorEventMapper = new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(NotificationEvent.class, DataOrchestratorEntity.class);
            }
        };
        dozerBeanMapper.addMapping(orchestratorEventMapper);

    }

    @Override
    public void close() throws Exception {
        this.workflowChannel.shutdown();
        this.drmsChannel.shutdown();
    }


    @Override
    public void run() {

        try {
            List<DataOrchestratorEntity> orchestratorEntityList = this.repository
                    .findAllEntitiesWithGivenStatus(EventStatus.DATA_ORCH_RECEIVED.name());
            Map<String, List<DataOrchestratorEntity>> entityMap = new HashMap<>();
            orchestratorEntityList.forEach(entity -> {
                entityMap.computeIfAbsent(entity.getResourceId(), list -> new ArrayList()).add(entity);
            });
            entityMap.forEach((key, value) -> {
                DataOrchestratorEntity entity = value.remove(0);
                processEvent(entity);
                entity.setEventStatus(EventStatus.WORKFLOW_LAUNCHED.name());
                repository.save(entity);
                value.forEach(val -> {
                    val.setEventStatus(EventStatus.DATA_ORCH_PROCESSED_AND_SKIPPED.name());
                    repository.save(val);
                });
            });
        } catch (Exception ex) {
            LOGGER.error("Error while processing events {}", ex);
        }

    }

    private void processEvent(DataOrchestratorEntity entity) {
        try {

            DRMSServiceAuthToken serviceAuthToken = DRMSServiceAuthToken.newBuilder()
                    .setAccessToken(entity.getAuthToken())
                    .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                    .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                            .setUsername(entity.getOwnerId())
                            .setTenantId(entity.getTenantId())
                            .build())
                    .build();

            StoragePreferenceFetchRequest storagePreferenceFetchRequest = StoragePreferenceFetchRequest.newBuilder()
                    .setStoragePreferenceId(entity.getStoragePreferenceId()).setAuthToken(serviceAuthToken).build();

            StoragePreferenceFetchResponse response = storagePreferenceServiceBlockingStub
                    .fetchStoragePreference(storagePreferenceFetchRequest);

            if (!response.hasStoragePreference()) {
                entity.setEventStatus(EventStatus.ERRORED.name());
                entity.setError("StoragePreference not found ");
                repository.save(entity);
                return;
            }

            //currently only ssh is working
            if (response.getStoragePreference().getSshStoragePreference().isInitialized()) {
                GenericResource genericResource = GenericResource
                        .newBuilder()
                        .setResourceId(entity.getResourceId())
                        .setResourceName(entity.getResourceName())
                        .setResourcePath(entity.getResourcePath())
                        .setType("FILE")
                        .setSshPreference(response.getStoragePreference().getSshStoragePreference()).build();
                ResourceCreateRequest resourceCreateRequest = ResourceCreateRequest
                        .newBuilder()
                        .setAuthToken(serviceAuthToken)
                        .setResource(genericResource)
                        .build();
                ResourceCreateResponse resourceCreateResponse = resourceServiceBlockingStub.createResource(resourceCreateRequest);
                GenericResource resource = resourceCreateResponse.getResource();

                WorkflowServiceAuthToken workflowServiceAuthToken = WorkflowServiceAuthToken.newBuilder().setAccessToken("").build();
                WorkflowMessage workflowMessage = WorkflowMessage.newBuilder().setResourceId(resource.getResourceId()).build();

                WorkflowInvocationRequest workflowInvocationRequest = WorkflowInvocationRequest
                        .newBuilder().setMessage(workflowMessage).setAuthToken(workflowServiceAuthToken).build();
                this.workflowServiceStub.invokeWorkflow(workflowInvocationRequest);
            }
        } catch (Exception exception) {
            LOGGER.error("Error occurred while processing outbound data orcehstrator event", exception);
            throw new RuntimeException(exception);
        }
    }


}
