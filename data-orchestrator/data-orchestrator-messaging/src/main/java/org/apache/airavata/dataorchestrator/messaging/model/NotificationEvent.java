package org.apache.airavata.dataorchestrator.messaging.model;

/**
 * Notification event represents triggering messages
 */
public class  NotificationEvent {

    public enum Type {
        REGISTER,
        CREATE,
        MODIFY,
        DELETE
    }

    private String resourcePath;
    private String resourceType;
    private Long occuredTime;
    private String authToken;
    private String tenantId;
    private String hostName;
    private String basePath;
    private Type eventType;

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
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

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public Type getEventType() {
        return eventType;
    }

    public void setEventType(Type eventType) {
        this.eventType = eventType;
    }
}
