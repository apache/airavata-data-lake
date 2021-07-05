package org.apache.airavata.dataorchestrator.messaging.model;

import org.apache.airavata.dataorchestrator.messaging.MessagingEvents;

import java.io.Serializable;
import java.util.UUID;

/**
 * Notification event represents triggering messages
 */
public class  NotificationEvent {
    private String resourcePath;
    private String resourceName;
    private String resourceType;
    private Context context;
    private String id;


    public NotificationEvent() {
        this.id = UUID.randomUUID().toString();

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
        private String authToken;
        private String tenantId;
        private String storagePreferenceId;
        private String basePath;


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

        public String getAuthToken() {
            return authToken;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getStoragePreferenceId() {
            return storagePreferenceId;
        }

        public void setStoragePreferenceId(String storagePreferenceId) {
            this.storagePreferenceId = storagePreferenceId;
        }

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }

    public String getResourceId() {
        return context.storagePreferenceId + ":" + resourcePath + ":" + resourceType;
    }


}
