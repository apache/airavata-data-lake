package org.apache.airavata.datalake.orchestrator.handlers;

import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.core.processor.MessageProcessor;
import org.apache.airavata.datalake.orchestrator.processor.EventProcessor;
import org.apache.airavata.dataorchestrator.messaging.consumer.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Orchestrator event handler
 */
@Component
public class OrchestratorEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorEventHandler.class);

    private Configuration configuration;

    private ExecutorService executorService;
    private MessageProcessor messageProcessor;
    private MessageConsumer messageConsumer;


    public OrchestratorEventHandler() {
    }

    public void init(Configuration configuration) {
        this.configuration = configuration;
        this.executorService = Executors.newFixedThreadPool(configuration.getEventProcessorWorkers());
        this.messageProcessor = new EventProcessor();
        this.messageProcessor.init();
        messageConsumer = new MessageConsumer(configuration.getConsumer().getBrokerURL(),
                configuration.getConsumer().getConsumerGroup(),
                configuration.getConsumer().getMaxPollRecordsConfig(),
                configuration.getConsumer().getTopic());
    }

    public void startProcessing() {
        messageConsumer.consume((notificationEvent -> {
            LOGGER.info("Message received " + notificationEvent.getResourceName());
            LOGGER.info("Submitting {} to process in thread pool", notificationEvent.getId());

        }));


    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }


}
