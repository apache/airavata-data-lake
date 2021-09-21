package org.apache.airavata.dataorchestrator.messaging.model;

import com.google.gson.Gson;
import com.google.protobuf.util.JsonFormat;
import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.Notification;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Notification event serializer
 */
public class NotificationSerializer implements Serializer<Notification> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationSerializer.class);

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, Notification notificationEvent) {
        try {
            return JsonFormat.printer().print(notificationEvent).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Failed to serialize message {}. So returning null", s, e);
            return null;
        }
    }

    @Override
    public void close() {

    }
}
