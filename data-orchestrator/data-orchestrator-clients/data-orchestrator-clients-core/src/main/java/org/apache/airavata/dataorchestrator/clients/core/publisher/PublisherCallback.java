package org.apache.airavata.dataorchestrator.clients.core.publisher;

import org.apache.kafka.clients.producer.RecordMetadata;

@FunctionalInterface
public interface PublisherCallback {

    /**
     * Handle response from Kafka producer
     * @param recordMetadata
     * @param exception
     */
    public void handleResponse(RecordMetadata recordMetadata, Exception exception);


}
