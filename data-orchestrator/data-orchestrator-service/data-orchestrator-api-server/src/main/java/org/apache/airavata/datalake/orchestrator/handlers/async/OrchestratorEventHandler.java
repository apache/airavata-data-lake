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

package org.apache.airavata.datalake.orchestrator.handlers.async;

import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.dataorchestrator.messaging.consumer.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
            LOGGER.info("Message received for resource path {}", notificationEvent.getResourcePath());
            try {
                this.executorService.submit(new OrchestratorEventProcessor(configuration, notificationEvent));
            } catch (Exception e) {
                LOGGER.error("Failed tu submit data orchestrator event to process on path {}",
                        notificationEvent.getResourcePath(), e);
            }
        }));
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }


}
