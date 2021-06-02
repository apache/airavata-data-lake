/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.datalake.orchestrator.workflow.engine.monitor.filter.mft;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaMFTMessageProducer {

    @org.springframework.beans.factory.annotation.Value("${kafka.url}")
    private String kafkaUrl;

    @org.springframework.beans.factory.annotation.Value("${kafka.mft.publisher.name}")
    private String publisherName;

    @org.springframework.beans.factory.annotation.Value("${kafka.mft.status.publish.topic}")
    private String topic;

    private Producer<String, DataTransferEvent> producer;

    public void init() {
        this.producer = createProducer();
    }

    private Producer<String, DataTransferEvent> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, publisherName);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                DataTransferEventSerializer.class.getName());
        return new KafkaProducer<String, DataTransferEvent>(props);
    }

    public void publish(DataTransferEvent event) throws ExecutionException, InterruptedException {
        final ProducerRecord<String, DataTransferEvent> record = new ProducerRecord<>(
                topic,
                event.getTaskId(),
                event);
        RecordMetadata recordMetadata = producer.send(record).get();
        producer.flush();
    }
}
