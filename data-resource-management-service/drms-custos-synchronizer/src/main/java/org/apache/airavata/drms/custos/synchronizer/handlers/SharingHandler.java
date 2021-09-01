package org.apache.airavata.drms.custos.synchronizer.handlers;

import org.apache.airavata.drms.custos.synchronizer.Configuration;
import org.apache.airavata.drms.custos.synchronizer.Utils;
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

    public SharingHandler() {

    }

    public void mergeSharings(Configuration configuration) {
        try {
            LOGGER.debug("Merging sharings for custos client with id " + configuration.getCustos().getCustosId());
            SharingManagementClient sharingManagementClient = Utils.getSharingManagementClient();
            mergeSharings(sharingManagementClient, configuration.getCustos().getTenantsToBeSynced());

        } catch (Exception ex) {
            ex.printStackTrace();
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
            ex.printStackTrace();
            String msg = "Error occurred while merging sharings from Custos, " + ex.getMessage();
            LOGGER.error(msg, ex);
        }


    }

    private void mergeEntities(Entity entity, String clientId) {
        String query = "Merge (u: " + entity.getType() + " {entityId: $entityId,"
                + "tenantId:$tenantId})"
                + " SET u += $props return u ";
        Map<String, Object> map = new HashMap<>();
        map.put("description", entity.getDescription());
        map.put("name", entity.getName());
        map.put("createdTime", entity.getCreatedAt());
        map.put("tenantId", clientId);
        map.put("entityId", entity.getId());
        map.put("entityType", entity.getType());
        map.put("lastModifiedTime", entity.getUpdatedAt());
        map.put("owner", entity.getOwnerId());
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("props", map);
        parameters.put("tenantId", clientId);
        parameters.put("entityId", entity.getId());
        try {
            Utils.getNeo4JConnector().runTransactionalQuery(parameters, query);
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = "Error occurred while merging entities, " + ex.getMessage();
            LOGGER.error(msg, ex);
        }

    }

    private void mergeEntityParentChildRelationShips(SharingManagementClient sharingManagementClient, Entity entity,
                                                     String clientId) {
        try {
            if (!entity.getParentId().trim().isEmpty()) {
                Entity parentEntity = Entity.newBuilder().setId(entity.getParentId()).build();
                Entity fullParentEntity = sharingManagementClient.getEntity(clientId, parentEntity);
                String query = "MATCH (a:" + entity.getType() + "), (b:" + fullParentEntity.getType() + ") WHERE a.entityId = $entityId" +
                        " AND a.tenantId = $tenantId  AND " + "b.entityId = $parentEntityId AND b.tenantId = $tenantId " +
                        "MERGE (a)-[r:CHILD_OF]->(b) RETURN a, b";
                Map<String, Object> map = new HashMap<>();
                map.put("entityId", entity.getId());
                map.put("parentEntityId", fullParentEntity.getId());
                map.put("tenantId", clientId);

                Utils.getNeo4JConnector().runTransactionalQuery(map, query);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = "Error occurred while merging parent child relationships ";
            LOGGER.error(msg, ex);
        }
    }

    private void mergeEntitySharings(SharingMetadata metadata, String clientId) {
        try {
        Entity entity = metadata.getEntity();
        String sourceId = metadata.getEntity().getId();
        String permissionId = metadata.getPermission().getId();
        String userId = metadata.getOwnerId();
        String type = metadata.getOwnerType();
        String sharedBy = metadata.getSharedBy();
        userId = userId.replaceAll("'", "`'");
        String query = null;
        if (type.equalsIgnoreCase("USER")) {
            query = "MATCH (a:" + entity.getType() + "), (b:User) WHERE a.entityId = $sourceId  AND a.tenantId = $clientId" +
                    " AND  b.username = $userId  AND b.tenantId = $clientId " +
                    "MERGE (a)-[r:SHARED_WITH]->(b) SET r += $props RETURN a, b";

        } else if (type.equalsIgnoreCase("GROUP")) {
            query = "MATCH (a:" + entity.getType() + "), (b:Group) WHERE a.entityId = $sourceId " +
                    " AND a.tenantId = $clientId  AND b.groupId = $userId  AND b.tenantId = $clientId " +
                    "MERGE (a)-[r:SHARED_WITH]->(b) SET r += $props RETURN a, b";
        }
        if (query != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("sourceId", sourceId);
            map.put("clientId", clientId);
            map.put("permissionId", permissionId);
            map.put("userId", userId);
            map.put("sharedBy", sharedBy);
            Map<String, Object> props = new HashMap<>();
            props.put("sharedBy", sharedBy);
            props.put("permission", permissionId);
            map.put("props", props);

                Utils.getNeo4JConnector().runTransactionalQuery(map, query);
        }
        }
        catch (Exception ex) {
                ex.printStackTrace();
                String msg = "Error occurred while merging sharings, " + ex.getMessage();
                LOGGER.error(msg, ex);
            }

    }

    public void deleteEntity(String entityId, String entityType, String clientId) {
        String query = "MATCH (e:" + entityType + ") WHERE e.entityId = $entityId " +
                " AND e.tenantId = $tenantId DETACH DELETE e";
        Map<String, Object> map = new HashMap<>();
        map.put("entityId", entityId);
        map.put("tenantId", clientId);
        try {
            Utils.getNeo4JConnector().runTransactionalQuery(map, query);
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
                    " AND e.tenantId = $tenantId AND b.username = $userId AND " +
                    " b.tenantId = $tenantId AND r.permission = $permission DELETE r";

        } else if (userType.equals("GROUP")) {
            query = "MATCH (e:" + entityType + ")-[r:SHARED_WITH]->(b:Group) WHERE e.entityId = $entityId " +
                    " AND e.tenantId = $tenantId AND b.groupId = $userId AND " +
                    " b.tenantId = $tenantId AND r.permission = $permission DELETE r";
        }
        Map<String, Object> map = new HashMap<>();
        map.put("entityId", entityId);
        map.put("tenantId", clientId);
        map.put("permission", permission);
        map.put("userId", userId);
        try {
            Utils.getNeo4JConnector().runTransactionalQuery(map, query);
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = "Error occurred while deleting entity, " + ex.getMessage();
            LOGGER.error(msg, ex);
        }

    }


}
