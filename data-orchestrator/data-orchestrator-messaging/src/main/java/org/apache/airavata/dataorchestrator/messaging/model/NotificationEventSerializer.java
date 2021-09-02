package org.apache.airavata.dataorchestrator.messaging.model;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;
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

        Gson gson = new Gson();
        return gson.toJson(notificationEvent).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void close() {

    }
}
