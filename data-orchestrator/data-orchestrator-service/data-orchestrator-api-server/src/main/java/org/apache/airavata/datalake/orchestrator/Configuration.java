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

        private long pollingDelay;
        private long pollingInterval;
        private int numOfEventsPerPoll;


        public OutboundEventProcessorConfig() {
        }

        public OutboundEventProcessorConfig(long pollingDelay, long pollingInterval) {
            this.pollingDelay = pollingDelay;
            this.pollingInterval = pollingInterval;
        }

        public long getPollingDelay() {
            return pollingDelay;
        }

        public void setPollingDelay(long pollingDelay) {
            this.pollingDelay = pollingDelay;
        }

        public long getPollingInterval() {
            return pollingInterval;
        }

        public void setPollingInterval(long pollingInterval) {
            this.pollingInterval = pollingInterval;
        }

        public int getNumOfEventsPerPoll() {
            return numOfEventsPerPoll;
        }

        public void setNumOfEventsPerPoll(int numOfEventsPerPoll) {
            this.numOfEventsPerPoll = numOfEventsPerPoll;
        }
    }


}
