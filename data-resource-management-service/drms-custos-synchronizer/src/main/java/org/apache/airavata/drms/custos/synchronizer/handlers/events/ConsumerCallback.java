package org.apache.airavata.drms.custos.synchronizer.handlers.events;

import org.apache.custos.messaging.service.Message;

@FunctionalInterface
public interface ConsumerCallback {

    void process(Message notificationEvent) throws Exception;

}
