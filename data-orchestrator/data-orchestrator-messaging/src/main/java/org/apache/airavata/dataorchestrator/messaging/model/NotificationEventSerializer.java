package org.apache.airavata.dataorchestrator.messaging.model;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Notification event serializer
 */
public class NotificationEventSerializer implements Serializer<NotificationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventSerializer.class);

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, NotificationEvent notificationEvent) {

        try {
            Gson gson = new Gson();
            notificationEvent.setOccuredTime(System.currentTimeMillis());
            return gson.toJson(notificationEvent).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Failed to serialize message {}. So returning null", s, e);
            return null;
        }
    }

    @Override
    public void close() {

    }
}
