package org.apache.airavata.dataorchestrator.messaging.model;

import org.apache.airavata.dataorchestrator.messaging.MessagingEvents;
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
        NotificationEvent.Context context = new NotificationEvent.Context();
        event.setId(parts[0]);
        context.setEvent(MessagingEvents.valueOf(parts[1]));
        context.setOccuredTime(Long.valueOf(parts[2]));
        context.setAuthToken(String.valueOf(parts[3]));
        context.setTenantId(String.valueOf(parts[4]));
        context.setStoragePreferenceId(parts[5]);
        context.setBasePath(parts[6]);
        event.setResourcePath(parts[7]);
        event.setResourceType(parts[8]);
        event.setResourceName(parts[9]);
        event.setContext(context);
        return event;
    }

    @Override
    public void close() {

    }
}
