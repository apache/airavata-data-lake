package org.apache.airavata.dataorchestrator.messaging.model;

import com.google.gson.Gson;
import com.google.protobuf.util.JsonFormat;
import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.Notification;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Notification event deserializer
 */
public class NotificationDeserializer implements Deserializer<Notification> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationDeserializer.class);

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public Notification deserialize(String topic, byte[] bytes) {
        String deserialized = new String(bytes);
        try {
            Notification.Builder notificationBuilder = Notification.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(deserialized, notificationBuilder);
            return notificationBuilder.build();
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize the message {}. So returning null", deserialized, e);
            return null;
        }
    }

    @Override
    public void close() {

    }
}
