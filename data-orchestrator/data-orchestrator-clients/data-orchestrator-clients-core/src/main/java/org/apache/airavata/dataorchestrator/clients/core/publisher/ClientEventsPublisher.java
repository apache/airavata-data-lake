package org.apache.airavata.dataorchestrator.clients.core.publisher;

import org.apache.airavata.dataorchestrator.messaging.model.NotificationMessage;

/**
 * Publish events to kafka queue that is listened by Kafka receiver
 */
public class ClientEventsPublisher {

    private static ClientEventsPublisher clientEventsPublisher = new ClientEventsPublisher();

    private ClientEventsPublisher() {

    }

    public void publish(NotificationMessage notificationMessage, PublisherCallback callback) {

    }

    public static ClientEventsPublisher getClientEventsPublisher() {
        return clientEventsPublisher;
    }
}
