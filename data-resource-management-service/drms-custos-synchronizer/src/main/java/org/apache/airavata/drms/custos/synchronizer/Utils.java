package org.apache.airavata.drms.custos.synchronizer;

import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.group.management.client.GroupManagementClient;
import org.apache.custos.sharing.management.client.SharingManagementClient;
import org.apache.custos.user.management.client.UserManagementClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static final Neo4JConnector neo4JConnector = new Neo4JConnector();
    private static CustosClientProvider custosClientProvider = null;
    private static SharingManagementClient sharingManagementClient;
    private static UserManagementClient userManagementClient;
    private static GroupManagementClient groupManagementClient;
    private static Configuration configuration;


    public static Configuration loadConfiguration(String path) {
        return Optional.ofNullable(path).
                map(Utils::loadConfig)
                .orElseThrow(() -> {
                    String msg = "Configuration path cannot be null";
                    LOGGER.error(msg);
                    throw new RuntimeException(msg);
                });
    }

    public static Configuration loadConfig(String filePath) {
        try (InputStream in = new FileInputStream(filePath)) {
            Yaml yaml = new Yaml();
            return yaml.loadAs(in, Configuration.class);
        } catch (Exception exception) {
            LOGGER.error("Error loading config file", exception);
        }
        return null;
    }

    public static void initializeConnectors(Configuration config) throws IOException {
        configuration = config;
        neo4JConnector.init(configuration.getDataResourceManagementService().getDbURI(),
                configuration.getDataResourceManagementService().getDbUser(),
                configuration.getDataResourceManagementService().getDbPassword());
        custosClientProvider = new CustosClientProvider.Builder()
                .setClientId(configuration.getCustos().getCustosId())
                .setClientSec(configuration.getCustos().getCustosSec())
                .setServerHost(configuration.getCustos().getHost())
                .setServerPort(configuration.getCustos().getPort())
                .build();
        sharingManagementClient = custosClientProvider.getSharingManagementClient();
        groupManagementClient = custosClientProvider.getGroupManagementClient();
        userManagementClient = custosClientProvider.getUserManagementClient();
    }

    public synchronized static Neo4JConnector getNeo4JConnector() {
        if (neo4JConnector.isOpen()) {
            return neo4JConnector;
        } else {
            neo4JConnector.init(configuration.getDataResourceManagementService().getDbURI(),
                    configuration.getDataResourceManagementService().getDbUser(),
                    configuration.getDataResourceManagementService().getDbPassword());
            return neo4JConnector;
        }

    }

    public synchronized static SharingManagementClient getSharingManagementClient() throws IOException {
        if (sharingManagementClient.isShutdown()) {
            return custosClientProvider.getSharingManagementClient();
        }
        return sharingManagementClient;
    }

    public synchronized static GroupManagementClient getGroupManagementClient() throws IOException {
        if (groupManagementClient.isShutdown()) {
            return custosClientProvider.getGroupManagementClient();
        }
        return groupManagementClient;
    }

    public synchronized static UserManagementClient getUserManagementClient() throws IOException {
        if (userManagementClient.isShutdown()) {
            return custosClientProvider.getUserManagementClient();
        }
        return userManagementClient;
    }

}
