package org.apache.airavata.dataorchestrator.messaging.consumer;

import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEventDeserializer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Kafka consumer
 */
public class MessageConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);

    private final Consumer<String, NotificationEvent> consumer;

    public MessageConsumer(String borkerURL, String consumerGroup, int maxPollRecordsConfig, String topic) {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, borkerURL);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, NotificationEventDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecordsConfig);

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public void consume(ConsumerCallback callback) {
        new Thread(() -> {

            while (true) {

                final ConsumerRecords<String, NotificationEvent> consumerRecords = consumer.poll(Long.MAX_VALUE);
                for (TopicPartition partition : consumerRecords.partitions()) {
                    List<ConsumerRecord<String, NotificationEvent>> partitionRecords = consumerRecords.records(partition);
                    LOGGER.info("Received data orchestrator records {}", partitionRecords.size());

                    for (ConsumerRecord<String, NotificationEvent> record : partitionRecords) {
                        try {
                            callback.process(record.value());
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }finally {
                            consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(record.offset() + 1)));
                        }
                    }
                }
            }
        }).start();
    }
}
