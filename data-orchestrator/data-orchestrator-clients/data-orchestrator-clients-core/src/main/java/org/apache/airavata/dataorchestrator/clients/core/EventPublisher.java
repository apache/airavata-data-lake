package org.apache.airavata.dataorchestrator.clients.core;

import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.Notification;

import java.util.concurrent.ExecutionException;

/**
 * A class responsible for process incoming notification events, callbacks from kafka producer, message replays
 */
public interface EventPublisher {
    public void publish(Notification notificationEvent, Notification.NotificationType eventType) throws Exception;
}
