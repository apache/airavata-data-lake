package org.apache.airavata.dataorchestrator.messaging.consumer;

import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;

@FunctionalInterface
public interface ConsumerCallback {

    void process(NotificationEvent notificationEvent) throws Exception;

}
