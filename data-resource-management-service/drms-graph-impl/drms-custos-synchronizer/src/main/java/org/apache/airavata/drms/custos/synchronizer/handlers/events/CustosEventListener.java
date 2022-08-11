package org.apache.airavata.drms.custos.synchronizer.handlers.events;


import org.apache.custos.messaging.service.Message;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class CustosEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustosEventListener.class);

    private final Consumer<String, Message> consumer;

    public CustosEventListener(String borkerURL, String consumerGroup, int maxPollRecordsConfig, String[] topics) {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, borkerURL);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MessageDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecordsConfig);

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays
                .stream(topics)
                .collect(Collectors.toList()));
    }

    public void consume(ConsumerCallback callback) {
        LOGGER.info("CustosEventListener started to listen on Custos events...");
        new Thread(() -> {

            while (true) {

                final ConsumerRecords<String, Message> consumerRecords = consumer.poll(Long.MAX_VALUE);
                for (TopicPartition partition : consumerRecords.partitions()) {
                    List<ConsumerRecord<String, Message>> partitionRecords = consumerRecords.records(partition);
                    LOGGER.info("Received custos records {}", partitionRecords.size());

                    for (ConsumerRecord<String, Message> record : partitionRecords) {


                        try {
                            callback.process(record.value());
                        } catch (Exception exception) {
                            LOGGER.info("Exception occurred in kafka listener ",exception.getMessage());
                        } finally {
                            consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(record.offset() + 1)));
                        }


                    }
                }
            }
        }).start();
    }


}
