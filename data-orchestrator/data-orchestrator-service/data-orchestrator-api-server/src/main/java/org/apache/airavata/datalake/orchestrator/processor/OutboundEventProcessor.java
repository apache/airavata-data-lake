package org.apache.airavata.datalake.orchestrator.processor;

import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.Utils;
import org.apache.airavata.datalake.orchestrator.connectors.DRMSConnector;
import org.apache.airavata.datalake.orchestrator.connectors.WorkflowServiceConnector;
import org.apache.airavata.datalake.orchestrator.core.processor.MessageProcessor;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEntity;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEventRepository;
import org.apache.airavata.datalake.orchestrator.registry.persistance.EventStatus;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;
import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class is responsible  and publish events to registry and
 * Workflow engine
 */
public class OutboundEventProcessor implements MessageProcessor<Configuration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutboundEventProcessor.class);

    private DozerBeanMapper dozerBeanMapper;
    private DataOrchestratorEventRepository repository;

    private DRMSConnector drmsConnector;
    private WorkflowServiceConnector workflowServiceConnector;

    public OutboundEventProcessor(Configuration configuration, DataOrchestratorEventRepository repository) throws Exception {
        this.repository = repository;
        this.init(configuration);
    }

    @Override
    public void init(Configuration configuration) throws Exception {
        this.drmsConnector = new DRMSConnector(configuration);
        this.workflowServiceConnector = new WorkflowServiceConnector(configuration);
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
        this.drmsConnector.close();
        this.workflowServiceConnector.close();
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

            String ownerId = entity.getOwnerId();
            String resourcePath = entity.getResourcePath();
            String tail = resourcePath.substring(resourcePath.indexOf(ownerId));
            String[] collections = tail.split("/");

//            Optional<String> optionalStorPref = drmsConnector.getSourceStoragePreferenceId(entity, entity.getHostName());
            Optional<String> optionalStorPref = null;
            if (optionalStorPref.isEmpty()) {
                entity.setEventStatus(EventStatus.ERRORED.name());
                entity.setError("StoragePreference not found for host: " + entity.getHostName());
                repository.save(entity);
                return;
            }

            String parentId = optionalStorPref.get();

            for (int i = 1; i < collections.length - 1; i++) {
                String resourceName = collections[i];
                String path = entity.getResourcePath().substring(0, entity.getResourcePath().indexOf(resourceName));
                path = path.concat(resourceName);
                String entityId = Utils.getId(path);
                Optional<GenericResource> optionalGenericResource =
                        this.drmsConnector.createResource(repository, entity, entityId, resourceName, path, parentId, "COLLECTION");
                if (optionalGenericResource.isPresent()) {
                    parentId = optionalGenericResource.get().getResourceId();

                } else {
                    entity.setEventStatus(EventStatus.ERRORED.name());
                    entity.setError("Collection structure creation failed: " + entity.getHostName());
                    repository.save(entity);
                    return;
                }
            }

            Optional<GenericResource> optionalGenericResource =
                    this.drmsConnector.createResource(repository, entity, entity.getResourceId(),
                            collections[collections.length - 1], entity.getResourcePath(),
                            parentId, "FILE");


            if (optionalGenericResource.isPresent()) {
                this.workflowServiceConnector.invokeWorkflow(repository, entity, optionalGenericResource.get());
                entity.setEventStatus(EventStatus.DISPATCHED_TO_WORFLOW_ENGING.name());
                repository.save(entity);
            } else {

            }
        } catch (Exception exception) {
            LOGGER.error("Error occurred while processing outbound data orchestrator event", exception);
            entity.setEventStatus(EventStatus.ERRORED.name());
            entity.setError("Error occurred while processing ");
            repository.save(entity);
        }
    }

}
