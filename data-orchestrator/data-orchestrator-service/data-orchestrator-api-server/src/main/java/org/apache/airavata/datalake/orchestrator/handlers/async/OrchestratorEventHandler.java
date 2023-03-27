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

import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.Notification;
import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.NotificationRegisterRequest;
import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.registry.persistance.entity.notification.NotificationEntity;
import org.apache.airavata.dataorchestrator.clients.core.NotificationClient;
import org.apache.airavata.dataorchestrator.messaging.consumer.MessageConsumer;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
    private MessageConsumer messageConsumer;
    private final Set<String> eventCache = new HashSet<>();
    private NotificationClient notificationClient;

    public OrchestratorEventHandler() {
    }

    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
        this.executorService = Executors.newFixedThreadPool(configuration.getEventProcessorWorkers());
        messageConsumer = new MessageConsumer(configuration.getConsumer().getBrokerURL(),
                configuration.getConsumer().getConsumerGroup(),
                configuration.getConsumer().getMaxPollRecordsConfig(),
                configuration.getConsumer().getTopic());
        this.notificationClient = new NotificationClient(
                configuration.getOutboundEventProcessor().getNotificationServiceHost(),
                configuration.getOutboundEventProcessor().getNotificationServicePort());
    }

    public void startProcessing() throws Exception {
        messageConsumer.consume((notificationEvent -> {
            LOGGER.info("Message received for resource path {} type {}", notificationEvent.getResourcePath(),
                    notificationEvent.getEventType());
            try {
                if (!eventCache.contains(notificationEvent.getResourcePath() + ":" + notificationEvent.getHostName())) {
                    eventCache.add(notificationEvent.getResourcePath() + ":" + notificationEvent.getHostName());

                    Notification.Builder notificationBuilder = Notification.newBuilder();
                    DozerBeanMapper mapper = new DozerBeanMapper();
                    mapper.map(notificationEvent, notificationBuilder);
                    notificationBuilder.setNotificationId(UUID.randomUUID().toString());

                    Notification notification = notificationBuilder.build();
                    LOGGER.info("Registering notification in the database");
                    this.notificationClient.get().registerNotification(NotificationRegisterRequest
                            .newBuilder().setNotification(notification).build());

                    this.executorService.submit(new OrchestratorEventProcessor(
                            configuration, notification, eventCache, notificationClient));
                } else {
                    LOGGER.info("Event is already processing");
                }


            } catch (Exception e) {
                LOGGER.error("Failed tu submit data orchestrator event to process on path {}",
                        notificationEvent.getResourcePath(), e);
            }
        }));
    }



    public void invokeMessageFlowForNotification(Notification notification) throws Exception {
        try {
            if (!eventCache.contains(notification.getResourcePath() + ":" + notification.getHostName())) {
                eventCache.add(notification.getResourcePath() + ":" + notification.getHostName());
                this.executorService.submit(new OrchestratorEventProcessor(
                        configuration, notification, eventCache, notificationClient));
            }
        }catch (Exception e) {
            LOGGER.error("Failed to submit data orchestrator event to process on path {}",
                    notification.getResourcePath(), e);
        }
    }



    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }


}
