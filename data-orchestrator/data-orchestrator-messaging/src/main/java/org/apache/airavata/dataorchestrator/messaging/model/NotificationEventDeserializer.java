package org.apache.airavata.dataorchestrator.messaging.model;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Notification event deserializer
 */
public class NotificationEventDeserializer implements Deserializer<NotificationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventDeserializer.class);

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public NotificationEvent deserialize(String topic, byte[] bytes) {
        String deserialized = new String(bytes);
        try {
            Gson gson = new Gson();
            return gson.fromJson(deserialized, NotificationEvent.class);
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize the message {}. So returning null", deserialized, e);
            return null;
        }
    }

    @Override
    public void close() {

    }
}
