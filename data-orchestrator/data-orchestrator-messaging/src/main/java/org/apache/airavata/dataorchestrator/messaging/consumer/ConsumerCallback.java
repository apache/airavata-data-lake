package org.apache.airavata.dataorchestrator.messaging.consumer;

import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.Notification;

@FunctionalInterface
public interface ConsumerCallback {

    void process(Notification notificationEvent) throws Exception;

}
