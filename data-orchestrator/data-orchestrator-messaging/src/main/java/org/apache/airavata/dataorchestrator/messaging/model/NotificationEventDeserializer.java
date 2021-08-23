package org.apache.airavata.dataorchestrator.messaging.model;

import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * Notification event deserializer
 */
public class NotificationEventDeserializer implements Deserializer<NotificationEvent> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public NotificationEvent deserialize(String topic, byte[] bytes) {
        String deserialized = new String(bytes);
        String parts[] = deserialized.split(",");
        NotificationEvent event = new NotificationEvent();
        event.setResourcePath(parts[0]);
        event.setResourceType(parts[1]);
        event.setOccuredTime(Long.valueOf(parts[2]));
        event.setTenantId(parts[3]);
        event.setHostName(parts[4]);
        event.setBasePath(parts[5]);
        event.setEventType(NotificationEvent.Type.valueOf(parts[6]));
        event.setAuthToken(parts[7]);
        return event;
    }

    @Override
    public void close() {

    }
}
