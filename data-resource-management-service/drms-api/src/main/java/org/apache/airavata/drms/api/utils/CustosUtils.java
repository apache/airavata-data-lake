package org.apache.airavata.drms.api.utils;

import org.apache.airavata.drms.core.constants.StorageConstants;
import org.apache.airavata.drms.core.constants.StoragePreferenceConstants;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.sharing.management.client.SharingManagementClient;
import org.apache.custos.sharing.service.Entity;
import org.apache.custos.sharing.service.EntityType;
import org.apache.custos.sharing.service.Status;

import java.io.IOException;
import java.util.Optional;

public class CustosUtils {


    public static void mergeStorageEntity(CustosClientProvider custosClientProvider, String tenantId, String storageId, String username) throws IOException {
        SharingManagementClient sharingManagementClient = custosClientProvider.getSharingManagementClient();
        EntityType entityType = EntityType.newBuilder().setId(StorageConstants.STORAGE_LABEL).build();
        EntityType type = sharingManagementClient.getEntityType(tenantId, entityType);
        if (!type.isInitialized() || type.getId().isEmpty()) {
            EntityType storEntityType = EntityType.newBuilder()
                    .setId(StorageConstants.STORAGE_LABEL)
                    .setName(StorageConstants.STORAGE_LABEL)
                    .setDescription("Storage entity type")
                    .build();
            sharingManagementClient.createEntityType(tenantId, storEntityType);
        }
        Entity entity = Entity.newBuilder().
                setId(storageId)
                .setName(storageId)
                .setOwnerId(username)
                .setType(StorageConstants.STORAGE_LABEL)
                .setDescription("Storage information").build();

        Status status = sharingManagementClient.isEntityExists(tenantId, entity);
        if (!status.getStatus()) {
            sharingManagementClient.createEntity(tenantId, entity);
        }
    }

    public static void deleteStorageEntity(CustosClientProvider custosClientProvider, String tenantId,
                                           String entityId) throws IOException {
        SharingManagementClient sharingManagementClient = custosClientProvider.getSharingManagementClient();
        Entity entity = Entity.newBuilder().
                setId(entityId).build();
        sharingManagementClient.deleteEntity(tenantId, entity);
    }

    public static void mergeStoragePreferenceEntity(CustosClientProvider custosClientProvider, String tenantId,
                                                    String storagePreferenceId, String storageId,
                                                    String username) throws IOException {
        SharingManagementClient sharingManagementClient = custosClientProvider.getSharingManagementClient();
        EntityType entityType = EntityType.newBuilder().setId(StoragePreferenceConstants.STORAGE_PREFERENCE_LABEL).build();
        EntityType type = sharingManagementClient.getEntityType(tenantId, entityType);
        CustosUtils.mergeStorageEntity(custosClientProvider, tenantId, storageId, username);
        if (!type.isInitialized() || type.getId().isEmpty()) {
            EntityType storEntityType = EntityType.newBuilder()
                    .setId(StoragePreferenceConstants.STORAGE_PREFERENCE_LABEL)
                    .setName(StoragePreferenceConstants.STORAGE_PREFERENCE_LABEL)
                    .setDescription("Storage preference entity type")
                    .build();
            sharingManagementClient.createEntityType(tenantId, storEntityType);
        }
        Entity entity = Entity.newBuilder().
                setId(storagePreferenceId)
                .setName(storagePreferenceId)
                .setOwnerId(username)
                .setParentId(storageId)
                .setType(StoragePreferenceConstants.STORAGE_PREFERENCE_LABEL)
                .setDescription("Storage Preference information").build();

        Status status = sharingManagementClient.isEntityExists(tenantId, entity);
        if (!status.getStatus()) {
            sharingManagementClient.createEntity(tenantId, entity);
        }
    }


    public static Optional<Entity> mergeResourceEntity(CustosClientProvider custosClientProvider, String tenantId, String storagePreferenceId,
                                                       String entityTypeId, String entityId, String entityName, String description, String username) throws IOException {
        SharingManagementClient sharingManagementClient = custosClientProvider.getSharingManagementClient();
        EntityType entityType = EntityType.newBuilder().setId(entityTypeId).build();
        EntityType type = sharingManagementClient.getEntityType(tenantId, entityType);
        if (!type.isInitialized() || type.getId().isEmpty()) {
            EntityType storEntityType = EntityType.newBuilder()
                    .setId(entityTypeId)
                    .setName(entityTypeId)
                    .setDescription("Resource  entity type " + entityTypeId)
                    .build();
            sharingManagementClient.createEntityType(tenantId, storEntityType);
        }
        Entity entity = Entity.newBuilder()
                .setId(entityId)
                .setName(entityName)
                .setOwnerId(username)
                .setParentId(storagePreferenceId)
                .setType(entityTypeId)
                .setDescription(description)
                .build();

        Status status = sharingManagementClient.isEntityExists(tenantId, entity);
        if (!status.getStatus()) {
            sharingManagementClient.createEntity(tenantId, entity);
            return Optional.ofNullable(sharingManagementClient.getEntity(tenantId, entity));
        }

        return Optional.empty();

    }


}
