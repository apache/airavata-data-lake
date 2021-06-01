package org.apache.airavata.dataorchestrator.messaging.publisher;

import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEventSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Publish events to kafka queue that is listened by Kafka receiver
 */
public class MessageProducer {

    private final Producer<String, NotificationEvent> producer;

    public MessageProducer(String borkerURL, String publisherId) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, borkerURL);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, publisherId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, NotificationEventSerializer.class.getName());
        this.producer = new KafkaProducer<String, NotificationEvent>(props);


    }

    public void publish(String topic, NotificationEvent notificationMessage, Callback callback) throws ExecutionException, InterruptedException {
        try {
            final ProducerRecord<String, NotificationEvent> record = new ProducerRecord<>(topic,
                    notificationMessage.getId(),
                    notificationMessage);
            producer.send(record, callback).get();
        } finally {
            producer.flush();
        }
    }

}
