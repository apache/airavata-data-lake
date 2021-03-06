package org.apache.airavata.datalake.orchestrator.handlers;

import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.processor.InboundEventProcessor;
import org.apache.airavata.datalake.orchestrator.processor.OutboundEventProcessor;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEventRepository;
import org.apache.airavata.dataorchestrator.messaging.consumer.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrator event handler
 */
@Component
public class OrchestratorEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorEventHandler.class);

    private Configuration configuration;

    private ExecutorService executorService;
    private ScheduledExecutorService ouboundExecutorService;
    private MessageConsumer messageConsumer;

    @Autowired
    private DataOrchestratorEventRepository dataOrchestratorEventRepository;


    public OrchestratorEventHandler() {
    }

    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
        this.executorService = Executors.newFixedThreadPool(configuration.getEventProcessorWorkers());
        this.ouboundExecutorService = Executors.newSingleThreadScheduledExecutor();
        messageConsumer = new MessageConsumer(configuration.getConsumer().getBrokerURL(),
                configuration.getConsumer().getConsumerGroup(),
                configuration.getConsumer().getMaxPollRecordsConfig(),
                configuration.getConsumer().getTopic());

    }

    public void startProcessing() throws Exception {
        messageConsumer.consume((notificationEvent -> {
            LOGGER.info("Message received " + notificationEvent.getResourceName());
            LOGGER.info("Submitting {} to process in thread pool", notificationEvent.getId());
            this.executorService.submit(new InboundEventProcessor(configuration, notificationEvent, dataOrchestratorEventRepository));

        }));

        this.ouboundExecutorService
                .scheduleAtFixedRate(new OutboundEventProcessor(configuration, dataOrchestratorEventRepository),
                        0, configuration.getOutboundEventProcessor().getPollingInterval(), TimeUnit.SECONDS);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }


}
