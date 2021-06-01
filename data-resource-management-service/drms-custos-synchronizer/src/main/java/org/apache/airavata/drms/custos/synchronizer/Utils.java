package org.apache.airavata.drms.custos.synchronizer;

import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.custos.clients.CustosClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static final Neo4JConnector neo4JConnector = new Neo4JConnector();
    private static CustosClientProvider custosClientProvider = null;


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

    public static void initializeConnectors(Configuration configuration) {
        neo4JConnector.init(configuration.getDataResourceManagementService().getDbURI(),
                configuration.getDataResourceManagementService().getDbUser(),
                configuration.getDataResourceManagementService().getDbPassword());
        LOGGER.info(configuration.getCustos().getCustosId());
        LOGGER.info(configuration.getCustos().getCustosSec());
        custosClientProvider = new CustosClientProvider.Builder()
                .setClientId(configuration.getCustos().getCustosId())
                .setClientSec(configuration.getCustos().getCustosSec())
                .setServerHost(configuration.getCustos().getHost())
                .setServerPort(configuration.getCustos().getPort())
                .build();
    }

    public static Neo4JConnector getNeo4JConnector() {
        return neo4JConnector;
    }

    public static CustosClientProvider getCustosClientProvider() {
        return custosClientProvider;
    }

}
