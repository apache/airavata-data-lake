package org.apache.airavata.dataorchestrator.clients.core;

import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.Notification;
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

    public void onRegistered(Notification event) throws Exception {
        LOGGER.info(" Registration event received for path " + event.getResourcePath());
        eventPublisher.publish(event, Notification.NotificationType.REGISTER);

    }

    public void onCreated(Notification event) throws Exception {
        LOGGER.info(event.getResourceType() + " " +
                event.getResourcePath() + ":" + event.getResourcePath() + " Created");
        eventPublisher.publish(event, Notification.NotificationType.CREATE);

    }

    public void onModified(Notification event) throws Exception {
        LOGGER.info(event.getResourceType() + " " +
                event.getResourcePath() + ":" + event.getResourcePath() + " Created");
        eventPublisher.publish(event, Notification.NotificationType.MODIFY);

    }

    public void onDeleted(Notification event) throws Exception {
        LOGGER.info(event.getResourceType() + " " +
                event.getResourcePath() + ":" + event.getBasePath() + " Created");
        eventPublisher.publish(event, Notification.NotificationType.DELETE);

    }


}
