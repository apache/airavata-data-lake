package org.apache.airavata.datalake.orchestrator.processor;

import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.core.adaptors.StorageAdaptor;
import org.apache.airavata.datalake.orchestrator.core.processor.MessageProcessor;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
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

    public InboundEventProcessor(Configuration configuration, NotificationEvent notificationEvent) throws Exception {
        this.configuration = configuration;
        this.notificationEvent = notificationEvent;
        this.init();
    }

    @Override
    public void init() throws Exception {
        try {
            Class<StorageAdaptor> storeClass = (Class<StorageAdaptor>) Class.
                    forName(this.configuration.getInMemoryStorageAdaptor());
            store = storeClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException |
                IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            LOGGER.error(" Error occurred while initiating Inbound event processor ", exception);
            throw exception;
        }

    }

    @Override
    public void run() {
        LOGGER.info("Inbound event processor received event "+notificationEvent.getResourceId());
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

        store.save(notificationEvent);

    }
}
