package org.apache.airavata.dataorchestrator.messaging.publisher;

import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.Notification;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Publish events to kafka queue that is listened by Kafka receiver
 */
public class MessageProducer {

    private final Producer<String, Notification> producer;

    public MessageProducer(String borkerURL, String publisherId) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, borkerURL);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, publisherId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, NotificationSerializer.class.getName());
        this.producer = new KafkaProducer<String, Notification>(props);


    }

    public void publish(String topic, Notification notificationMessage, Callback callback) throws ExecutionException, InterruptedException {
        try {
            final ProducerRecord<String, Notification> record = new ProducerRecord<>(topic,
                    notificationMessage.getResourcePath(),
                    notificationMessage);
            producer.send(record, callback).get();
        } finally {
            producer.flush();
        }
    }

}
