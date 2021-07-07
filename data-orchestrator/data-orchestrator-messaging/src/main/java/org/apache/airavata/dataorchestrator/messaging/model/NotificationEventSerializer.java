package org.apache.airavata.dataorchestrator.messaging.model;

import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * Notification event serializer
 */
public class NotificationEventSerializer implements Serializer<NotificationEvent> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, NotificationEvent notificationEvent) {
        String serializedData = notificationEvent.getId() + "," +
                notificationEvent.getContext().getEvent().name() + "," +
                notificationEvent.getContext().getOccuredTime() + "," +
                notificationEvent.getContext().getAuthToken() + "," +
                notificationEvent.getContext().getTenantId() + "," +
                notificationEvent.getContext().getHostName() + "," +
                notificationEvent.getContext().getBasePath() + "," +
                notificationEvent.getResourcePath() + "," +
                notificationEvent.getResourceType() + "," +
                notificationEvent.getResourceName();
        return serializedData.getBytes();
    }

    @Override
    public void close() {

    }
}
