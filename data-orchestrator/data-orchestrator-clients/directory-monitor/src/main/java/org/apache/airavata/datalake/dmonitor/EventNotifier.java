package org.apache.airavata.datalake.dmonitor;

import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.Notification;
import org.apache.airavata.dataorchestrator.messaging.publisher.MessageProducer;

import java.io.File;
import java.nio.file.Paths;

public class EventNotifier {
    private String tenantId;
    private String resourceType;
    private String hostName;
    private String eventType;
    private String authToken;
    private String kafkaEventTopic;
    private MessageProducer kafkaProducer;

    public void notify(String resourcePath) throws Exception {
        kafkaProducer.publish(kafkaEventTopic, Notification.newBuilder()
                .setTenantId(tenantId)
                .setBasePath(Paths.get(resourcePath).getParent().toAbsolutePath()
                        .getParent().toAbsolutePath().toString() + File.separator)
                .setResourceType(resourceType)
                .setHostName(hostName)
                .setEventType(Notification.NotificationType.valueOf(eventType))
                .setAuthToken(authToken)
                .setResourcePath(resourcePath).build(), (metadata, exception) -> {});
    }


    public static final class EventNotifierBuilder {
        private String tenantId;
        private String resourceType;
        private String hostName;
        private String eventType;
        private String authToken;
        private String kafkaUrl;
        private String kafkaPublisherId;
        private String kafkaEventTopic;

        private EventNotifierBuilder() {
        }

        public static EventNotifierBuilder newBuilder() {
            return new EventNotifierBuilder();
        }

        public EventNotifierBuilder withTenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public EventNotifierBuilder withResourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public EventNotifierBuilder withHostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public EventNotifierBuilder withEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public EventNotifierBuilder withAuthToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public EventNotifierBuilder withKafkaUrl(String kafkaUrl) {
            this.kafkaUrl = kafkaUrl;
            return this;
        }

        public EventNotifierBuilder withKafkaPublisherId(String kafkaPublisherId) {
            this.kafkaPublisherId = kafkaPublisherId;
            return this;
        }

        public EventNotifierBuilder withKafkaEventTopic(String kakfaEventTopic) {
            this.kafkaEventTopic = kakfaEventTopic;
            return this;
        }

        public EventNotifier build() {
            EventNotifier eventNotifier = new EventNotifier();
            eventNotifier.resourceType = this.resourceType;
            eventNotifier.authToken = this.authToken;
            eventNotifier.hostName = this.hostName;
            eventNotifier.kafkaEventTopic = this.kafkaEventTopic;
            eventNotifier.eventType = this.eventType;
            eventNotifier.tenantId = this.tenantId;
            eventNotifier.kafkaProducer = new MessageProducer(kafkaUrl, kafkaPublisherId);
            return eventNotifier;
        }
    }
}
