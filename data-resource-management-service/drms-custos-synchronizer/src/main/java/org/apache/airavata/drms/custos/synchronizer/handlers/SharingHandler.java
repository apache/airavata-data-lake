package org.apache.airavata.drms.custos.synchronizer.handlers;

import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.airavata.drms.custos.synchronizer.Configuration;
import org.apache.airavata.drms.custos.synchronizer.Utils;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.sharing.management.client.SharingManagementClient;
import org.apache.custos.sharing.service.Entity;
import org.apache.custos.sharing.service.GetAllDirectSharingsResponse;
import org.apache.custos.sharing.service.SharingMetadata;
import org.apache.custos.sharing.service.SharingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharingHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SharingHandler.class);

    private final Neo4JConnector neo4JConnector;
    private CustosClientProvider custosClientProvider;

    public SharingHandler() {
        this.neo4JConnector = Utils.getNeo4JConnector();
        this.custosClientProvider = Utils.getCustosClientProvider();
    }

    public void mergeSharings(Configuration configuration) {
        try {
            LOGGER.debug("Merging sharings for custos client with id "+ configuration.getCustos().getCustosId());
            SharingManagementClient sharingManagementClient = custosClientProvider.getSharingManagementClient();
            mergeSharings(sharingManagementClient, configuration.getCustos().getTenantsToBeSynced());

        } catch (Exception ex) {
            String msg = "Exception occurred while merging user" + ex.getMessage();
            LOGGER.error(msg, ex);
        }

    }


    private void mergeSharings(SharingManagementClient sharingManagementClient, String[] clientIds) {
        try {
            SharingRequest sharingRequest = SharingRequest.newBuilder().build();
            Arrays.stream(clientIds).forEach(clientId -> {
                GetAllDirectSharingsResponse response = sharingManagementClient
                        .getAllDirectSharings(clientId, sharingRequest);
                List<SharingMetadata> metadataList = response.getSharedDataList();
                metadataList.forEach(metadata -> {
                    mergeEntities(metadata.getEntity(), clientId);

                });
                metadataList.forEach(metadata -> {
                    mergeEntityParentChildRelationShips(sharingManagementClient, metadata.getEntity(), clientId);
                    mergeEntitySharings(metadata, clientId);
                });
            });

        } catch (Exception ex) {
            String msg = "Error occurred while merging sharings from Custos ";
            LOGGER.error(msg, ex);
        }


    }

    private void mergeEntities(Entity entity, String clientId) {
        String query = "Merge (u:" + entity.getType() + "{entityId: '" + entity.getId() + "',"
                + "custosClientId:'" + clientId + "'}" + ")"
                + " SET u = $props return u ";
        Map<String, Object> map = new HashMap<>();
        map.put("description", entity.getDescription());
        map.put("name", entity.getName());
        map.put("createdTime", entity.getCreatedAt());
        map.put("custosClientId", clientId);
        map.put("entityId", entity.getId());
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("props", map);
        try {
            this.neo4JConnector.runTransactionalQuery(parameters, query);
        } catch (Exception ex) {
            String msg = "Error occurred while merging entities ";
            LOGGER.error(msg, ex);
        }

    }

    private void mergeEntityParentChildRelationShips(SharingManagementClient sharingManagementClient, Entity entity,
                                                     String clientId) {

        if (!entity.getParentId().trim().isEmpty()) {
            Entity parentEntity = Entity.newBuilder().setId(entity.getParentId()).build();
            Entity fullParentEntity = sharingManagementClient.getEntity(clientId, parentEntity);
            String query = "MATCH (a:" + entity.getType() + "), (b:" + fullParentEntity.getType() + ") WHERE a.entityId = '"
                    + entity.getId() + "' AND a.custosClientId = '"
                    + clientId + "' AND " + "b.entityId ='" + fullParentEntity.getId() + "' AND b.custosClientId ='" + clientId +
                    "' MERGE (a)-[r:CHILD_OF]->(b) RETURN a, b";
            try {
                this.neo4JConnector.runTransactionalQuery(query);
            } catch (Exception ex) {
                String msg = "Error occurred while merging parent child relationships ";
                LOGGER.error(msg, ex);
            }
        }
    }

    private void mergeEntitySharings(SharingMetadata metadata, String clientId) {
        Entity entity = metadata.getEntity();
        String sourceId = metadata.getEntity().getId();
        String permissionId = metadata.getPermission().getId();
        String userId = metadata.getOwnerId();
        String type = metadata.getOwnerType();
        String query = null;
        if (type.equalsIgnoreCase("USER")) {
            query = "MATCH (a:" + entity.getType() + "), (b:User) WHERE a.entityId = '"
                    + sourceId + "' AND a.custosClientId = '"
                    + clientId + "' AND " + "b.username ='" + userId + "' AND b.custosClientId ='" + clientId +
                    "' MERGE (a)-[r:SHARED_WITH]->(b) SET r.permission='" + permissionId + "' RETURN a, b";

        } else if (type.equalsIgnoreCase("GROUP")) {
            query = "MATCH (a:" + entity.getType() + "), (b:Group) WHERE a.entityId = '"
                    + sourceId + "' AND a.custosClientId = '"
                    + clientId + "' AND " + "b.groupId ='" + userId + "' AND b.custosClientId ='" + clientId +
                    "' MERGE (a)-[r:SHARED_WITH]->(b) SET r.permission='" + permissionId + "' RETURN a, b";
        }
        if (query != null) {
            try {
                this.neo4JConnector.runTransactionalQuery(query);
            } catch (Exception ex) {
                String msg = "Error occurred while merging sharings ";
                LOGGER.error(msg, ex);
            }
        }

    }
}
