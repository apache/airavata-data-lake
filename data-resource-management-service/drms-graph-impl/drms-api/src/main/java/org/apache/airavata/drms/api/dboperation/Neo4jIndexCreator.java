package org.apache.airavata.drms.api.dboperation;

import org.apache.airavata.drms.api.handlers.ResourceServiceHandler;
import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.custos.clients.CustosClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class Neo4jIndexCreator {

    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceHandler.class);

    @Autowired
    private Neo4JConnector neo4JConnector;

    @Autowired
    private CustosClientProvider custosClientProvider;

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        logger.info("Running neo4j Index creation..........");
        String userIndex = "CREATE INDEX user_index_username IF NOT EXISTS FOR (n:User) ON (n.username, n.tenantId)";
        String fileIndex = "CREATE INDEX user_index_username IF NOT EXISTS FOR (n:FILE) ON (n.entityId, n.tenantId)";
        String collectionIndex = "CREATE INDEX collection_index_username IF NOT EXISTS FOR (n:COLLECTION) ON (n.entityId, n.tenantId)";
        String storageIndex = "CREATE INDEX storage_index IF NOT EXISTS FOR (n:Storage) ON (n.entityId, n.tenantId)";

        neo4JConnector.runTransactionalQuery(userIndex);
        neo4JConnector.runTransactionalQuery(fileIndex);
        neo4JConnector.runTransactionalQuery(collectionIndex);
        neo4JConnector.runTransactionalQuery(storageIndex);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createConstraints() {
        logger.info("Running neo4j constraints creation..........");
        String userConstraint = "CREATE CONSTRAINT IF NOT EXISTS ON   (n:User)  ASSERT n.username   IS UNIQUE";
        String groupConstraint = "CREATE CONSTRAINT IF NOT EXISTS ON   (n:Group)  ASSERT n.groupId   IS UNIQUE";
        String resourceConstraint = "CREATE CONSTRAINT IF NOT EXISTS ON   (n:COLLECTION)  ASSERT n.entityId   IS UNIQUE";
        String fileConstraint = "CREATE CONSTRAINT IF NOT EXISTS ON   (n:FILE)  ASSERT n.entityId   IS UNIQUE";
        String storageConstraint = "CREATE CONSTRAINT IF NOT EXISTS ON   (n:Storage)  ASSERT n.entityId   IS UNIQUE";
        String storagePreferenceConstraint = "CREATE CONSTRAINT IF NOT EXISTS ON   (n:StoragePreference)  ASSERT n.entityId   IS UNIQUE";
        neo4JConnector.runTransactionalQuery(userConstraint);
        neo4JConnector.runTransactionalQuery(groupConstraint);
        neo4JConnector.runTransactionalQuery(resourceConstraint);
        neo4JConnector.runTransactionalQuery(fileConstraint);
        neo4JConnector.runTransactionalQuery(storageConstraint);
        neo4JConnector.runTransactionalQuery(storagePreferenceConstraint);
    }

}
