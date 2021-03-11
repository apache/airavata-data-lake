package org.apache.airavata.datalake.metadata.db.service.backend.neo4j;

import org.apache.airavata.datalake.metadata.db.service.backend.Connector;
import org.apache.airavata.datalake.metadata.db.service.exceptions.DBConnectorException;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

public class Neo4JConnector implements Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4JConnector.class);

    private SessionFactory factory;

    @Value("${spring.neo4j.uri}")
    private String neo4JURI;


    @Override
    public boolean init() {
        try {
            LOGGER.info("Initializing Neo4J connector ......");
            Configuration configuration = new Configuration.Builder()
                    .uri(neo4JURI)
                    .build();
            factory =
                    new SessionFactory(configuration, "org.apache.airavata.datalake.metadata.db.service");
            return true;
        } catch (Exception ex) {
            String msg = "Neo4J Connection Failure, " + ex.getMessage();
            LOGGER.error(msg);
            throw new DBConnectorException(msg, ex);
        }
    }

    @Override
    public void close() throws IOException {
        if (factory != null) {
            factory.close();
        }
    }

    public Session openConnection() {
        return factory.openSession();
    }


}
