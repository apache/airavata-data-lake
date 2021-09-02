package org.apache.airavata.dataorchestrator.messaging.model;

import com.google.gson.Gson;
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
        Gson gson = new Gson();
        return gson.fromJson(deserialized, NotificationEvent.class);
    }

    @Override
    public void close() {

    }
}
