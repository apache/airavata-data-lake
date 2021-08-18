package org.apache.airavata.datalake.orchestrator.processor;

import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.Utils;
import org.apache.airavata.datalake.orchestrator.core.processor.MessageProcessor;
import org.apache.airavata.datalake.orchestrator.registry.persistance.entity.DataOrchestratorEntity;
import org.apache.airavata.datalake.orchestrator.registry.persistance.entity.OwnershipEntity;
import org.apache.airavata.datalake.orchestrator.registry.persistance.repository.DataOrchestratorEventRepository;
import org.apache.airavata.datalake.orchestrator.registry.persistance.entity.EventStatus;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;
import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for pick events from kafka queue and publish them into inmemory store
 */
public class InboundEventProcessor implements MessageProcessor<Configuration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InboundEventProcessor.class);
    private Configuration configuration;
    private NotificationEvent notificationEvent;
    private DozerBeanMapper dozerBeanMapper;

    private DataOrchestratorEventRepository repository;

    public InboundEventProcessor(Configuration configuration, NotificationEvent notificationEvent,
                                 DataOrchestratorEventRepository repository) throws Exception {
        this.notificationEvent = notificationEvent;
        this.repository = repository;
        this.init(configuration);
    }

    @Override
    public void init(Configuration configuration) throws Exception {
        try {
            this.configuration = configuration;
            dozerBeanMapper = new DozerBeanMapper();
            BeanMappingBuilder orchestratorEventMapper = new BeanMappingBuilder() {
                @Override
                protected void configure() {
                    mapping(NotificationEvent.class, DataOrchestratorEntity.class);
                }
            };
            dozerBeanMapper.addMapping(orchestratorEventMapper);
        } catch (Exception exception) {
            LOGGER.error(" Error occurred while initiating Inbound event processor ", exception);
            throw exception;
        }

    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public void run() {
        try {
            LOGGER.info("Inbound event processor received event " + notificationEvent.getResourceId());
            String typeStr = this.configuration.getMessageFilter().getResourceType();
            String[] allowedTypes = typeStr.split(",");
            boolean proceed = false;
            long size = Arrays.stream(allowedTypes).filter(type ->
                    type.equals(notificationEvent.getResourceType())).count();
            if (size == 0) {
                return;
            }
            String eventTypeStr = this.configuration.getMessageFilter().getEventType();
            String[] eventTypes = eventTypeStr.split(",");
            long eventSize = Arrays.stream(eventTypes).filter(type ->
                    type.trim().equals(notificationEvent.getContext().getEvent().name())).count();
            if (eventSize == 0) {
                return;
            }

            String pattern = this.configuration.getMessageFilter().getResourceNameExclusions();

            // Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(notificationEvent.getResourceName());

            if (m.find()) {
                return;
            }

            DataOrchestratorEntity entity = createEntity(notificationEvent);
            repository.save(entity);
        } catch (Exception exception) {
            LOGGER.error("Error occurred while pre processing event {}", this.notificationEvent.getResourceId(), exception);
        }

    }

    private DataOrchestratorEntity createEntity(NotificationEvent event) throws NoSuchAlgorithmException {
        DataOrchestratorEntity entity = dozerBeanMapper.map(event, DataOrchestratorEntity.class);
        entity.setOccurredTime(new Date(event.getContext().getOccuredTime()));
        entity.setEventStatus(EventStatus.DATA_ORCH_RECEIVED.name());
        entity.setEventType(event.getContext().getEvent().name());
        entity.setAuthToken(event.getContext().getAuthToken());
        entity.setHostName(event.getContext().getHostName());

        String resourcePath = event.getResourcePath();
        String basePath = event.getContext().getBasePath();
        String removeBasePath = resourcePath.substring(basePath.length());
        String[] splitted = removeBasePath.split("/");

        OwnershipEntity owner1 = new OwnershipEntity();
        owner1.setId(UUID.randomUUID().toString());
        owner1.setUserId(splitted[1]);
        owner1.setPermissionId("OWNER");
        owner1.setDataOrchestratorEntity(entity);

        OwnershipEntity owner2 = new OwnershipEntity();
        owner2.setId(UUID.randomUUID().toString());
        owner2.setUserId(splitted[0]);
        owner2.setPermissionId("ADMIN");
        owner2.setDataOrchestratorEntity(entity);


        Set<OwnershipEntity> owners = new HashSet<>();
        owners.add(owner1);
        owners.add(owner2);

        entity.setOwnershipEntities(owners);

        entity.setTenantId(event.getContext().getTenantId());

        String authDecoded = new String(Base64.getDecoder()
                .decode(event.getContext().getAuthToken().getBytes(StandardCharsets.UTF_8)));
        String agentId = authDecoded.split(":")[0];
        entity.setAgentId(agentId);
        entity.setResourceId(Utils.getId(event.getResourceId()));
        return entity;
    }
}
