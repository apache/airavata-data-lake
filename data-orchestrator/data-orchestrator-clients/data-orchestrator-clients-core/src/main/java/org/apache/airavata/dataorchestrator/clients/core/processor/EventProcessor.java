package org.apache.airavata.dataorchestrator.clients.core.processor;

import org.apache.airavata.dataorchestrator.clients.core.model.NotificationEvent;
import org.apache.airavata.dataorchestrator.messaging.MessagingEvents;

/**
 * A class responsible for process incoming notification events, callbacks from kafka producer, message replays
 */
public class EventProcessor {

    public static EventProcessor eventProcessor = new EventProcessor();

    private EventProcessor() {
    }

    public void publish(NotificationEvent notificationEvent, MessagingEvents event){

    }


    public static EventProcessor getEventProcessor() {
        return eventProcessor;
    }

}
