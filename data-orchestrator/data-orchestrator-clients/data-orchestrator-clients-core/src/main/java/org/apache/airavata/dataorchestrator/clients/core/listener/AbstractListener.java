package org.apache.airavata.dataorchestrator.clients.core.listener;

import org.apache.airavata.dataorchestrator.clients.core.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EventListener;

/**
 * Abstract class for data orchestrator clients event listeners.
 */
public abstract class AbstractListener implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractListener.class);


    public void onRegistered(NotificationEvent event) {
        LOGGER.info("Calling register method ");

    }

    public void onCreated(NotificationEvent event) {
        LOGGER.info("Calling onCreated method ");

    }

    public void onModified(NotificationEvent event) {
        LOGGER.info("Calling onModified method ");

    }

    public void onDeleted(NotificationEvent event) {
        LOGGER.info("Calling onDeleted method ");

    }


}
