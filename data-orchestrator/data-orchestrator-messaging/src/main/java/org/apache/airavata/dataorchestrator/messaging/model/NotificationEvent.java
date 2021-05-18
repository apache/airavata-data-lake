package org.apache.airavata.dataorchestrator.messaging.model;

import org.apache.airavata.dataorchestrator.messaging.MessagingEvents;

import java.io.Serializable;
import java.util.UUID;

/**
 * Notification event represents triggering messages
 */
public class NotificationEvent {

    private String host;
    private int port;
    private String protocol;
    private String resourcePath;
    private String resourceName;
    private String resourceType;
    private Context context;
    private String id;

    public NotificationEvent() {
        this.id = UUID.randomUUID().toString();

    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static class Context implements Serializable {

        private MessagingEvents event;
        private Long occuredTime;


        public MessagingEvents getEvent() {
            return event;
        }

        public void setEvent(MessagingEvents event) {
            this.event = event;
        }

        public Long getOccuredTime() {
            return occuredTime;
        }

        public void setOccuredTime(Long occuredTime) {
            this.occuredTime = occuredTime;
        }
    }

    public String getResourceId() {
        return host + ":" + resourcePath + ":" + resourceType;
    }


}
