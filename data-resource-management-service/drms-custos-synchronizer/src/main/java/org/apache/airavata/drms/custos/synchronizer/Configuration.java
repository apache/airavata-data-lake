package org.apache.airavata.drms.custos.synchronizer;

public class Configuration {

    private long pollingInterval;
    private Custos custos;
    private DataResourceManagementService dataResourceManagementService;

    public Configuration() {

    }

    public Custos getCustos() {
        return custos;
    }

    public void setCustos(Custos custos) {
        this.custos = custos;
    }

    public DataResourceManagementService getDataResourceManagementService() {
        return dataResourceManagementService;
    }

    public void setDataResourceManagementService(DataResourceManagementService dataResourceManagementService) {
        this.dataResourceManagementService = dataResourceManagementService;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public static class Custos {

        private String host;
        private int port;
        private String custosId;
        private String custosSec;
        private String[] tenantsToBeSynced;
        private String custosBrokerURL;
        private String consumerGroup;
        private int maxPollRecordsConfig;
        private String[] topics;

        public Custos(String host, int port, String custosId, String custosSec, String[] tenantsToBeSynced,
                      String custosBrokerURL, String consumerGroup, int maxPollRecordsConfig, String[] topics) {
            this.host = host;
            this.port = port;
            this.custosId = custosId;
            this.custosSec = custosSec;
            this.tenantsToBeSynced = tenantsToBeSynced;
            this.custosBrokerURL = custosBrokerURL;
            this.consumerGroup = consumerGroup;
            this.maxPollRecordsConfig = maxPollRecordsConfig;
            this.topics = topics;
        }

        public Custos() {

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

        public String getCustosId() {
            return custosId;
        }

        public void setCustosId(String custosId) {
            this.custosId = custosId;
        }

        public String getCustosSec() {
            return custosSec;
        }

        public void setCustosSec(String custosSec) {
            this.custosSec = custosSec;
        }

        public String[] getTenantsToBeSynced() {
            return tenantsToBeSynced;
        }

        public void setTenantsToBeSynced(String[] tenantsToBeSynced) {
            this.tenantsToBeSynced = tenantsToBeSynced;
        }

        public String getCustosBrokerURL() {
            return custosBrokerURL;
        }

        public void setCustosBrokerURL(String custosBrokerURL) {
            this.custosBrokerURL = custosBrokerURL;
        }

        public String getConsumerGroup() {
            return consumerGroup;
        }

        public void setConsumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
        }

        public int getMaxPollRecordsConfig() {
            return maxPollRecordsConfig;
        }

        public void setMaxPollRecordsConfig(int maxPollRecordsConfig) {
            this.maxPollRecordsConfig = maxPollRecordsConfig;
        }

        public String[] getTopics() {
            return topics;
        }

        public void setTopics(String[] topics) {
            this.topics = topics;
        }
    }

    public static class DataResourceManagementService {

        private String dbURI;
        private String dbUser;
        private String dbPassword;

        public DataResourceManagementService(String dbURI, String dbUser, String dbPassword) {
            this.dbURI = dbURI;
            this.dbUser = dbUser;
            this.dbPassword = dbPassword;
        }

        public DataResourceManagementService() {
        }

        public String getDbURI() {
            return dbURI;
        }

        public void setDbURI(String dbURI) {
            this.dbURI = dbURI;
        }

        public String getDbUser() {
            return dbUser;
        }

        public void setDbUser(String dbUser) {
            this.dbUser = dbUser;
        }

        public String getDbPassword() {
            return dbPassword;
        }

        public void setDbPassword(String dbPassword) {
            this.dbPassword = dbPassword;
        }
    }


}
