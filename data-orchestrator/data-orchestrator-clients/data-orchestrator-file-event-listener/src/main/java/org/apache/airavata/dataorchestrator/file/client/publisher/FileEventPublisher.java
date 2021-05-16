package org.apache.airavata.dataorchestrator.file.client.publisher;

import org.apache.airavata.dataorchestrator.clients.core.EventPublisher;
import org.apache.airavata.dataorchestrator.file.client.model.Configuration;
import org.apache.airavata.dataorchestrator.messaging.MessagingEvents;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;
import org.apache.airavata.dataorchestrator.messaging.publisher.MessageProducer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class FileEventPublisher implements EventPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileEventPublisher.class);

    private Configuration configuration;
    private MessageProducer messageProducer;

    public FileEventPublisher(Configuration configuration) {
        this.configuration = configuration;
        this.messageProducer = new MessageProducer(configuration.getProducer().getBrokerURL(),
                configuration.getProducer().getPublisherId());
    }

    @Override
    public void publish(NotificationEvent notificationEvent, MessagingEvents event) throws ExecutionException, InterruptedException {
        notificationEvent.getContext().setEvent(event);
        messageProducer.publish(configuration.getProducer().getPublisherTopic(), notificationEvent, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
             LOGGER.info(" Complete message publishing");
            }
        });
    }
}
