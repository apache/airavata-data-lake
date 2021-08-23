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
        String serializedData = notificationEvent.getResourcePath() + "," +
                notificationEvent.getResourceType() + "," +
                notificationEvent.getOccuredTime() + "," +
                notificationEvent.getTenantId() + "," +
                notificationEvent.getHostName() + "," +
                notificationEvent.getBasePath() + "," +
                notificationEvent.getEventType() + "," +
                notificationEvent.getAuthToken();
        return serializedData.getBytes();
    }

    @Override
    public void close() {

    }
}
