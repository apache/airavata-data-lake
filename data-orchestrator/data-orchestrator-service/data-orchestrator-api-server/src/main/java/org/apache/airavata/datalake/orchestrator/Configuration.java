package org.apache.airavata.datalake.orchestrator;

public class Configuration {

    private int eventProcessorWorkers;

    private Consumer consumer;

    private String inMemoryStorageAdaptor;

    private FilterConfig messageFilter;

    private OutboundEventProcessorConfig outboundEventProcessor;

    public FilterConfig getMessageFilter() {
        return messageFilter;
    }

    public void setMessageFilter(FilterConfig messageFilter) {
        this.messageFilter = messageFilter;
    }

    public TenantConfigs tenantConfigs;

    public Configuration() {

    }

    public Configuration(int eventProcessorWorkers) {
        this.eventProcessorWorkers = eventProcessorWorkers;
    }

    public int getEventProcessorWorkers() {
        return eventProcessorWorkers;
    }

    public void setEventProcessorWorkers(int eventProcessorWorkers) {
        this.eventProcessorWorkers = eventProcessorWorkers;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public String getInMemoryStorageAdaptor() {
        return inMemoryStorageAdaptor;
    }

    public void setInMemoryStorageAdaptor(String inMemoryStorageAdaptor) {
        this.inMemoryStorageAdaptor = inMemoryStorageAdaptor;
    }

    public OutboundEventProcessorConfig getOutboundEventProcessor() {
        return outboundEventProcessor;
    }

    public void setOutboundEventProcessor(OutboundEventProcessorConfig outboundEventProcessor) {
        this.outboundEventProcessor = outboundEventProcessor;
    }

    public TenantConfigs getTenantConfigs() {
        return tenantConfigs;
    }

    public void setTenantConfigs(TenantConfigs tenantConfigs) {
        this.tenantConfigs = tenantConfigs;
    }

    public static class Consumer {

        private String brokerURL;
        private String consumerGroup;
        private String topic;
        private int maxPollRecordsConfig;

        public Consumer(String brokerURL, String consumerGroup, String topic) {
            this.brokerURL = brokerURL;
            this.consumerGroup = consumerGroup;
            this.topic = topic;
        }

        public Consumer() {

        }

        public String getBrokerURL() {
            return brokerURL;
        }

        public void setBrokerURL(String brokerURL) {
            this.brokerURL = brokerURL;
        }

        public String getConsumerGroup() {
            return consumerGroup;
        }

        public void setConsumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public int getMaxPollRecordsConfig() {
            return maxPollRecordsConfig;
        }

        public void setMaxPollRecordsConfig(int maxPollRecordsConfig) {
            this.maxPollRecordsConfig = maxPollRecordsConfig;
        }
    }

    public static class FilterConfig {

        private String resourceType;
        private String eventType;
        private String resourceNameExclusions;

        public FilterConfig() {

        }

        public String getResourceType() {
            return resourceType;
        }

        public void setResourceType(String resourceType) {
            this.resourceType = resourceType;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getResourceNameExclusions() {
            return resourceNameExclusions;
        }

        public void setResourceNameExclusions(String resourceNameExclusions) {
            this.resourceNameExclusions = resourceNameExclusions;
        }
    }


    public static class OutboundEventProcessorConfig {

        private String workflowEngineHost;
        private int workflowPort;
        private String drmsHost;
        private int drmsPort;
        private int pollingInterval;
        private String mftHost;
        private int mftPort;
        private String notificationServiceHost;
        private int notificationServicePort;

        public OutboundEventProcessorConfig() {}

        public String getWorkflowEngineHost() {
            return workflowEngineHost;
        }

        public void setWorkflowEngineHost(String workflowEngineHost) {
            this.workflowEngineHost = workflowEngineHost;
        }

        public int getWorkflowPort() {
            return workflowPort;
        }

        public void setWorkflowPort(int workflowPort) {
            this.workflowPort = workflowPort;
        }

        public String getDrmsHost() {
            return drmsHost;
        }

        public void setDrmsHost(String drmsHost) {
            this.drmsHost = drmsHost;
        }

        public int getDrmsPort() {
            return drmsPort;
        }

        public void setDrmsPort(int drmsPort) {
            this.drmsPort = drmsPort;
        }

        public int getPollingInterval() {
            return pollingInterval;
        }

        public void setPollingInterval(int pollingInterval) {
            this.pollingInterval = pollingInterval;
        }

        public String getMftHost() {
            return mftHost;
        }

        public void setMftHost(String mftHost) {
            this.mftHost = mftHost;
        }

        public int getMftPort() {
            return mftPort;
        }

        public void setMftPort(int mftPort) {
            this.mftPort = mftPort;
        }

        public String getNotificationServiceHost() {
            return notificationServiceHost;
        }

        public void setNotificationServiceHost(String notificationServiceHost) {
            this.notificationServiceHost = notificationServiceHost;
        }

        public int getNotificationServicePort() {
            return notificationServicePort;
        }

        public void setNotificationServicePort(int notificationServicePort) {
            this.notificationServicePort = notificationServicePort;
        }
    }


    public static class TenantConfigs {

        private String tenantId;
        private String userGroup;
        private String adminGroup;

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getUserGroup() {
            return userGroup;
        }

        public void setUserGroup(String userGroup) {
            this.userGroup = userGroup;
        }

        public String getAdminGroup() {
            return adminGroup;
        }

        public void setAdminGroup(String adminGroup) {
            this.adminGroup = adminGroup;
        }
    }


}
