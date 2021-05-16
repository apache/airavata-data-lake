package org.apache.airavata.datalake.orchestrator;

public class Configuration {

    private int eventProcessorWorkers;

    private Consumer consumer;

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


}
