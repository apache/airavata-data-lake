package org.apache.airavata.dataorchestrator.file.client.model;

public class Configuration {
    private String listeningPath;
    private String hostName;

    private Producer producer;

    private Custos custos;

    public Configuration() {
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getListeningPath() {
        return listeningPath;
    }

    public void setListeningPath(String listeningPath) {
        this.listeningPath = listeningPath;
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    public Custos getCustos() {
        return custos;
    }

    public void setCustos(Custos custos) {
        this.custos = custos;
    }

    public static class Producer {
        private String brokerURL;
        private String publisherId;
        private String publisherTopic;

        public String getBrokerURL() {
            return brokerURL;
        }

        public void setBrokerURL(String brokerURL) {
            this.brokerURL = brokerURL;
        }

        public String getPublisherId() {
            return publisherId;
        }

        public void setPublisherId(String publisherId) {
            this.publisherId = publisherId;
        }

        public String getPublisherTopic() {
            return publisherTopic;
        }

        public void setPublisherTopic(String publisherTopic) {
            this.publisherTopic = publisherTopic;
        }
    }

    public static class Custos {

        private String serviceAccountId;
        private String serviceAccountSecret;
        private String tenantId;

        public String getServiceAccountId() {
            return serviceAccountId;
        }

        public void setServiceAccountId(String serviceAccountId) {
            this.serviceAccountId = serviceAccountId;
        }

        public String getServiceAccountSecret() {
            return serviceAccountSecret;
        }

        public void setServiceAccountSecret(String serviceAccountSecret) {
            this.serviceAccountSecret = serviceAccountSecret;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }
    }


}
