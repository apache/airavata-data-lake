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
    }

    public void mergeSharings(Configuration configuration) {
        try {
            LOGGER.debug("Merging sharings for custos client with id " + configuration.getCustos().getCustosId());
            SharingManagementClient sharingManagementClient = Utils.getSharingManagementClient();
            mergeSharings(sharingManagementClient, configuration.getCustos().getTenantsToBeSynced());

        } catch (Exception ex) {
            String msg = "Exception occurred while merging user, " + ex.getMessage();
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
            String msg = "Error occurred while merging sharings from Custos, " + ex.getMessage();
            LOGGER.error(msg, ex);
        }


    }

    private void mergeEntities(Entity entity, String clientId) {
        String query = "Merge (u: " + entity.getType() + " {entityId: $entityId,"
                + "custosClientId:$custosClientId})"
                + " SET u = $props return u ";
        Map<String, Object> map = new HashMap<>();
        map.put("description", entity.getDescription());
        map.put("name", entity.getName());
        map.put("createdTime", entity.getCreatedAt());
        map.put("custosClientId", clientId);
        map.put("entityId", entity.getId());
        map.put("entityType", entity.getType());
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("props", map);
        parameters.put("custosClientId", clientId);
        parameters.put("entityId", entity.getId());
        try {
            this.neo4JConnector.runTransactionalQuery(parameters, query);
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = "Error occurred while merging entities, " + ex.getMessage();
            LOGGER.error(msg, ex);
        }

    }

    private void mergeEntityParentChildRelationShips(SharingManagementClient sharingManagementClient, Entity entity,
                                                     String clientId) {

        if (!entity.getParentId().trim().isEmpty()) {
            Entity parentEntity = Entity.newBuilder().setId(entity.getParentId()).build();
            Entity fullParentEntity = sharingManagementClient.getEntity(clientId, parentEntity);
            String query = "MATCH (a:" + entity.getType() + "), (b:" + fullParentEntity.getType() + ") WHERE a.entityId = $entityId" +
                    "AND a.custosClientId = $custosClientId  AND " + "b.entityId = $parentEntityId AND b.custosClientId = $custosClientId " +
                    "MERGE (a)-[r:CHILD_OF]->(b) RETURN a, b";
            Map<String, Object> map = new HashMap<>();
            map.put("entityId", entity.getId());
            map.put("parentEntityId", fullParentEntity.getId());
            map.put("custosClientId", clientId);
            try {
                this.neo4JConnector.runTransactionalQuery(map, query);
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
        userId = userId.replaceAll("'", "`'");
        String query = null;
        if (type.equalsIgnoreCase("USER")) {
            query = "MATCH (a:" + entity.getType() + "), (b:User) WHERE a.entityId = $sourceId  AND a.custosClientId = $clientId" +
                    " AND  b.username = $userId  AND b.custosClientId = $clientId " +
                    "MERGE (a)-[r:SHARED_WITH]->(b) SET r.permission= $permissionId  RETURN a, b";

        } else if (type.equalsIgnoreCase("GROUP")) {
            query = "MATCH (a:" + entity.getType() + "), (b:Group) WHERE a.entityId = $sourceId " +
                    " AND a.custosClientId = $clientId  AND b.groupId = $userId  AND b.custosClientId = $clientId " +
                    "MERGE (a)-[r:SHARED_WITH]->(b) SET r.permission= $permissionId RETURN a, b";
        }
        if (query != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("sourceId", sourceId);
            map.put("clientId", clientId);
            map.put("permissionId", permissionId);
            map.put("userId", userId);
            try {
                this.neo4JConnector.runTransactionalQuery(map, query);
            } catch (Exception ex) {
                ex.printStackTrace();
                String msg = "Error occurred while merging sharings, " + ex.getMessage();
                LOGGER.error(msg, ex);
            }
        }

    }

    public void deleteEntity(String entityId, String entityType, String clientId) {
        String query = "MATCH (e:" + entityType + ") WHERE e.entityId = $entityId " +
                " AND e.custosClientId = $custosClientId DETACH DELETE e";
        Map<String, Object> map = new HashMap<>();
        map.put("entityId", entityId);
        map.put("custosClientId", clientId);
        try {
            this.neo4JConnector.runTransactionalQuery(map, query);
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = "Error occurred while deleting entity, " + ex.getMessage();
            LOGGER.error(msg, ex);
        }

    }

    public void deleteEntitySharings(String entityId, String entityType, String userType, String userId,
                                     String permission, String clientId) {
        String query = null;
        if (userType.equals("USER")) {
            query = "MATCH (e:" + entityType + ")-[r:SHARED_WITH]->(b:User) WHERE e.entityId = $entityId " +
                    " AND e.custosClientId = $custosClientId AND b.username = $userId AND " +
                    " b.custosClientId = $custosClientId AND r.permission = $permission DELETE r";

        } else if (userType.equals("GROUP")) {
            query = "MATCH (e:" + entityType + ")-[r:SHARED_WITH]->(b:Group) WHERE e.entityId = $entityId " +
                    " AND e.custosClientId = $custosClientId AND b.groupId = $userId AND " +
                    " b.custosClientId = $custosClientId AND r.permission = $permission DELETE r";
        }
        Map<String, Object> map = new HashMap<>();
        map.put("entityId", entityId);
        map.put("custosClientId", clientId);
        map.put("permission", permission);
        map.put("userId", userId);
        try {
            this.neo4JConnector.runTransactionalQuery(query);
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = "Error occurred while deleting entity, " + ex.getMessage();
            LOGGER.error(msg, ex);
        }

    }


}
