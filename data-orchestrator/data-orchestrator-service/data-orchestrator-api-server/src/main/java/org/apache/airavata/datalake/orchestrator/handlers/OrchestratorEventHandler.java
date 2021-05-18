package org.apache.airavata.datalake.orchestrator.handlers;

import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.core.processor.MessageProcessor;
import org.apache.airavata.datalake.orchestrator.db.persistance.DataOrchestratorEventRepository;
import org.apache.airavata.datalake.orchestrator.processor.InboundEventProcessor;
import org.apache.airavata.datalake.orchestrator.processor.OutboundEventProcessor;
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
    private MessageProcessor messageProcessor;
    private MessageConsumer messageConsumer;
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private DataOrchestratorEventRepository dataOrchestratorEventRepository;


    public OrchestratorEventHandler() {
    }

    public void init(Configuration configuration) {
        this.configuration = configuration;
        this.executorService = Executors.newFixedThreadPool(configuration.getEventProcessorWorkers());
        messageConsumer = new MessageConsumer(configuration.getConsumer().getBrokerURL(),
                configuration.getConsumer().getConsumerGroup(),
                configuration.getConsumer().getMaxPollRecordsConfig(),
                configuration.getConsumer().getTopic());
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void startProcessing() throws Exception {
        messageConsumer.consume((notificationEvent -> {
            LOGGER.info("Message received " + notificationEvent.getResourceName());
            LOGGER.info("Submitting {} to process in thread pool", notificationEvent.getId());
            this.executorService.submit(new InboundEventProcessor(configuration, notificationEvent));

        }));

        this.scheduledExecutorService
                .scheduleAtFixedRate(new OutboundEventProcessor(configuration, dataOrchestratorEventRepository),
                        configuration.getOutboundEventProcessor().getPollingDelay(),
                        configuration.getOutboundEventProcessor().getPollingInterval(),
                        TimeUnit.MILLISECONDS);

    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }


}
