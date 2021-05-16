package org.apache.airavata.dataorchestrator.clients.core;

import org.apache.airavata.dataorchestrator.messaging.MessagingEvents;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EventListener;

/**
 * Abstract class for data orchestrator clients event listeners.
 */
public abstract class AbstractListener implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractListener.class);

    private EventPublisher eventPublisher;

    public AbstractListener(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void onRegistered(NotificationEvent event) throws Exception {
        LOGGER.info(" Registration event received for path " + event.getResourcePath());
        eventPublisher.publish(event, MessagingEvents.REGISTER);

    }

    public void onCreated(NotificationEvent event) throws Exception {
        LOGGER.info(event.getResourceType() + " " +
                event.getResourcePath() + ":" + event.getResourceName() + " Created");
        eventPublisher.publish(event, MessagingEvents.CREATE);

    }

    public void onModified(NotificationEvent event) throws Exception {
        LOGGER.info(event.getResourceType() + " " +
                event.getResourcePath() + ":" + event.getResourceName() + " Created");
        eventPublisher.publish(event, MessagingEvents.MODIFIED);

    }

    public void onDeleted(NotificationEvent event) throws Exception {
        LOGGER.info(event.getResourceType() + " " +
                event.getResourcePath() + ":" + event.getResourceName() + " Created");
        eventPublisher.publish(event, MessagingEvents.DELETE);

    }


}
