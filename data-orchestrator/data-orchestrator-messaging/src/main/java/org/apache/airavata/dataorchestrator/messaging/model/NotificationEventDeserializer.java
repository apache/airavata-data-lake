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
        event.setHost(parts[3]);
        event.setPort(Integer.parseInt(parts[4]));
        event.setProtocol(parts[5]);
        event.setResourcePath(parts[6]);
        event.setResourceType(parts[7]);
        event.setResourceName(parts[8]);
        return event;
    }

    @Override
    public void close() {

    }
}
