package org.apache.airavata.datalake.orchestrator.processor;

import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.core.adaptors.StorageAdaptor;
import org.apache.airavata.datalake.orchestrator.core.processor.MessageProcessor;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEntity;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEventRepository;
import org.apache.airavata.datalake.orchestrator.registry.persistance.EventStatus;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;
import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for pick events from kafka queue and publish them into inmemory store
 */
public class InboundEventProcessor implements MessageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(InboundEventProcessor.class);
    private StorageAdaptor store;

    private Configuration configuration;
    private NotificationEvent notificationEvent;
    private DozerBeanMapper dozerBeanMapper;

    private DataOrchestratorEventRepository repository;

    public InboundEventProcessor(Configuration configuration, NotificationEvent notificationEvent,
                                 DataOrchestratorEventRepository repository) throws Exception {
        this.configuration = configuration;
        this.notificationEvent = notificationEvent;
        this.repository = repository;
        this.init();
    }

    @Override
    public void init() throws Exception {
        try {
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
        entity.setStoragePreferenceId(event.getContext().getStoragePreferenceId());

        String resourcePath = event.getResourcePath();
        String basePath = event.getContext().getBasePath();
        String removeBasePath = resourcePath.substring(basePath.length());
        String[] splitted = removeBasePath.split("/");
        String ownerId = splitted[0];
        entity.setOwnerId(ownerId);

        entity.setTenantId(event.getContext().getTenantId());

        String authDecoded = new String(Base64.getDecoder()
                .decode(event.getContext().getAuthToken().getBytes(StandardCharsets.UTF_8)));
        String agentId = authDecoded.split(":")[0];
        entity.setAgentId(agentId);
        entity.setResourceId(getResourceId(event.getResourceId()));
        return entity;
    }

    private String getResourceId(String message) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        byte[] array = md.digest(message.getBytes(StandardCharsets.UTF_8));
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, array);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
}
