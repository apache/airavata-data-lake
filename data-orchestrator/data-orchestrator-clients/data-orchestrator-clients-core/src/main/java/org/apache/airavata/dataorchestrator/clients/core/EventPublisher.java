package org.apache.airavata.dataorchestrator.clients.core;

import org.apache.airavata.dataorchestrator.messaging.MessagingEvents;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;

import java.util.concurrent.ExecutionException;

/**
 * A class responsible for process incoming notification events, callbacks from kafka producer, message replays
 */
public interface EventPublisher {


    public void publish(NotificationEvent notificationEvent, MessagingEvents event) throws Exception;


}
