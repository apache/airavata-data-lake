package org.apache.airavata.dataorchestrator.file.client.model;

public class Configuration {
    private String fileServerHost;
    private int fileServerPort;
    private String fileServerProtocol;
    private String listeningPath;

    private Producer producer;

    public Configuration() {
    }


    public String getFileServerHost() {
        return fileServerHost;
    }

    public void setFileServerHost(String fileServerHost) {
        this.fileServerHost = fileServerHost;
    }

    public int getFileServerPort() {
        return fileServerPort;
    }

    public void setFileServerPort(int fileServerPort) {
        this.fileServerPort = fileServerPort;
    }

    public String getFileServerProtocol() {
        return fileServerProtocol;
    }

    public void setFileServerProtocol(String fileServerProtocol) {
        this.fileServerProtocol = fileServerProtocol;
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
}
