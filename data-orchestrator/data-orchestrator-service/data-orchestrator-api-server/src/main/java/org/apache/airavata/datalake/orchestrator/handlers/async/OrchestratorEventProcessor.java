/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.datalake.orchestrator.handlers.async;

import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.Notification;
import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.NotificationStatus;
import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.NotificationStatusRegisterRequest;
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.drms.storage.AnyStoragePreference;
import org.apache.airavata.datalake.drms.storage.TransferMapping;
import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.Utils;
import org.apache.airavata.datalake.orchestrator.connectors.DRMSConnector;
import org.apache.airavata.datalake.orchestrator.connectors.WorkflowServiceConnector;
import org.apache.airavata.dataorchestrator.clients.core.NotificationClient;
import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.DirectoryMetadataResponse;
import org.apache.airavata.mft.api.service.FetchResourceMetadataRequest;
import org.apache.airavata.mft.api.service.FileMetadataResponse;
import org.apache.airavata.mft.api.service.MFTApiServiceGrpc;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.common.DelegateAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OrchestratorEventProcessor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorEventProcessor.class);

    private final Notification notification;

    private final DRMSConnector drmsConnector;
    private final Configuration configuration;
    private final WorkflowServiceConnector workflowServiceConnector;
    private final Set<String> eventCache;
    private final NotificationClient notificationClient;

    public OrchestratorEventProcessor(Configuration configuration, Notification notificationEvent,
                                      Set<String> eventCache, NotificationClient notificationClient) throws Exception {
        this.notification = notificationEvent;
        this.eventCache = eventCache;
        this.drmsConnector = new DRMSConnector(configuration);
        this.workflowServiceConnector = new WorkflowServiceConnector(configuration);
        this.configuration = configuration;
        this.notificationClient = notificationClient;
    }

    private List<GenericResource> createResourceWithParentDirectories(String hostName, String storageId, String basePath,
                                                                      String resourcePath, String resourceType, String user,
                                                                      Map<String, GenericResource> resourceCache)
            throws Exception {

        List<GenericResource> resourceList = new ArrayList<>();

        String parentType = "Storage";

        String[] splitted = resourcePath.substring(basePath.length()).split("/");

        String currentPath = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        String parentId = storageId;
        for (int i = 0; i < splitted.length - 1; i++) {
            String resourceName = splitted[i];
            currentPath = currentPath + "/" + resourceName;

            /*if (resourceCache.containsKey(currentPath)) {
                resourceList.add(resourceCache.get(currentPath));
                parentId = resourceCache.get(currentPath).getResourceId();
                logger.info("Using cached resource with path {} for path {}", currentPath,
                        resourceCache.get(currentPath).getResourcePath());
                continue;
            }*/

            String resourceId = Utils.getId(storageId + ":" + currentPath);
            Optional<GenericResource> optionalGenericResource =
                    this.drmsConnector.createResource(notification.getAuthToken(),
                            notification.getTenantId(),
                            resourceId, resourceName, currentPath, parentId, "COLLECTION", parentType, user);
            if (optionalGenericResource.isPresent()) {
                parentId = optionalGenericResource.get().getResourceId();
                parentType = "COLLECTION";

                Map<String, String> metadata = new HashMap<>();
                metadata.put("resourcePath", currentPath);
                metadata.put("hostName", hostName);
                this.drmsConnector.addResourceMetadata(notification.getAuthToken(),
                        notification.getTenantId(), parentId, user, parentType, metadata);

                resourceCache.put(currentPath, optionalGenericResource.get());
                resourceList.add(optionalGenericResource.get());
            } else {
                logger.error("Could not create a resource for path {}", currentPath);
                throw new Exception("Could not create a resource for path " + currentPath);
            }
        }

        currentPath = currentPath + "/" + splitted[splitted.length - 1];

        Optional<GenericResource> optionalGenericResource =
                this.drmsConnector.createResource(notification.getAuthToken(),
                        notification.getTenantId(),
                        Utils.getId(storageId + ":" + currentPath),
                        splitted[splitted.length - 1], currentPath,
                        parentId, resourceType, parentType, user);

        if (optionalGenericResource.isPresent()) {
            GenericResource genericResource = optionalGenericResource.get();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("resourcePath", currentPath);
            metadata.put("hostName", hostName);
            this.drmsConnector.addResourceMetadata(notification.getAuthToken(),
                    notification.getTenantId(), genericResource.getResourceId(), user, resourceType, metadata);

            resourceList.add(genericResource);
        } else {
            logger.error("Could not create a resource for path {}", currentPath);
            throw new Exception("Could not create a resource for path " + currentPath);
        }

        return resourceList;
    }


    private void shareResourcesWithUsers(List<GenericResource> resourceList, String admin, String user, String permission) throws Exception {
        for (GenericResource resource : resourceList) {
            logger.info("Sharing resource {} with path {} with user {}",
                    resource.getResourceId(), resource.getResourcePath(), user);
            this.drmsConnector.shareWithUser(notification.getAuthToken(), notification.getTenantId(),
                    admin, user, resource.getResourceId(), permission);
        }
    }

    private void shareResourcesWithGroups(List<GenericResource> resourceList, String admin, String group, String permission) throws Exception {
        for (GenericResource resource : resourceList) {
            logger.info("Sharing resource {} with path {} with group {}",
                    resource.getResourceId(), resource.getResourcePath(), group);
            this.drmsConnector.shareWithGroup(notification.getAuthToken(), notification.getTenantId(),
                    admin, group, resource.getResourceId(), permission);
        }
    }

    @Override
    public void run() {
        logger.info("Processing resource path {} on storage {}", notification.getResourcePath(),
                notification.getBasePath());

        Map<String, GenericResource> resourceCache = new HashMap<>();
        try {

            this.notificationClient.get().registerNotificationStatus(NotificationStatusRegisterRequest.newBuilder()
                    .setStatus(NotificationStatus.newBuilder()
                            .setStatusId(UUID.randomUUID().toString())
                            .setNotificationId(notification.getNotificationId())
                            .setStatus(NotificationStatus.StatusType.DATA_ORCH_RECEIVED)
                            .setDescription("Notification Received")
                            .setPublishedTime(System.currentTimeMillis())
                            .build()).build());

            if (!"FOLDER".equals(notification.getResourceType())) {
                logger.error("Resource {} should be a Folder type but got {}",
                        notification.getResourcePath(),
                        notification.getResourceType());
                logger.error("Resource should be a Folder type");
            }
            String removeBasePath = notification.getResourcePath().substring(notification.getBasePath().length());
            String[] splitted = removeBasePath.split("/");

            String adminUser = splitted[0];
            String owner = splitted[1].split("_")[0];

            Map<String, String> ownerRules = new HashMap<>();
            ownerRules.put(adminUser, "VIEWER");
            ownerRules.put(splitted[1], "OWNER");

            Optional<TransferMapping> optionalTransferMapping = drmsConnector.getActiveTransferMapping(
                    notification.getAuthToken(),
                    notification.getTenantId(), adminUser,
                    notification.getHostName());

            if (optionalTransferMapping.isEmpty()) {
                logger.error("Could not find a transfer mapping for user {} and host {}", adminUser, notification.getHostName());
                throw new Exception("Could not find a transfer mapping");
            }

            TransferMapping transferMapping = optionalTransferMapping.get();

            String sourceStorageId = transferMapping.getSourceStorage().getSshStorage().getStorageId();
            String sourceHostName = transferMapping.getSourceStorage().getSshStorage().getHostName();
            String destinationStorageId = transferMapping.getDestinationStorage().getSshStorage().getStorageId();
            String destinationHostName = transferMapping.getDestinationStorage().getSshStorage().getHostName();

            // Creating parent resource

            List<GenericResource> resourceList = createResourceWithParentDirectories(sourceHostName, sourceStorageId,
                    notification.getBasePath(),
                    notification.getResourcePath(),
                    "COLLECTION", adminUser, resourceCache);

            shareResourcesWithUsers(Collections.singletonList(resourceList.get(resourceList.size() - 1)),
                    adminUser, owner, "VIEWER");

            shareResourcesWithGroups(Collections.singletonList(resourceList.get(0)), adminUser,
                    configuration.getTenantConfigs().getAdminGroup(),
                    "EDITOR");

            GenericResource resourceObj = resourceList.get(resourceList.size() - 1);

            Optional<AnyStoragePreference> sourceSPOp = this.drmsConnector.getStoragePreference(
                    notification.getAuthToken(), adminUser,
                    notification.getTenantId(), sourceStorageId);

            if (sourceSPOp.isEmpty()) {
                logger.error("No storage preference found for source storage {} and user {}", sourceStorageId, adminUser);
                throw new Exception("No storage preference found for source storage");
            }

            Optional<AnyStoragePreference> destSPOp = this.drmsConnector.getStoragePreference(
                    notification.getAuthToken(), adminUser,
                    notification.getTenantId(), destinationStorageId);

            if (destSPOp.isEmpty()) {
                logger.error("No storage preference found for destination storage {} and user {}", sourceStorageId, adminUser);
                throw new Exception("No storage preference found for destination storage");
            }

            AnyStoragePreference sourceSP = sourceSPOp.get();
            AnyStoragePreference destSP = destSPOp.get();

            String decodedAuth = new String(Base64.getDecoder().decode(notification.getAuthToken()));
            String[] authParts = decodedAuth.split(":");

            if (authParts.length != 2) {
                throw new Exception("Could not decode auth token to work with MFT");
            }

            DelegateAuth delegateAuth = DelegateAuth.newBuilder()
                    .setUserId(adminUser)
                    .setClientId(authParts[0])
                    .setClientSecret(authParts[1])
                    .putProperties("TENANT_ID", notification.getTenantId()).build();

            AuthToken mftAuth = AuthToken.newBuilder().setDelegateAuth(delegateAuth).build();
            List<String> resourceIDsToProcess = new ArrayList<>();

            // Fetching file list for parent resource
            scanResourceForChildResources(resourceObj, mftAuth, sourceSP, sourceStorageId, sourceHostName,
                    adminUser, resourceIDsToProcess, resourceCache, 4);

            logger.info("Creating destination zip resource for directory {}", notification.getResourcePath());
            resourceList = createResourceWithParentDirectories(destinationHostName, destinationStorageId, notification.getBasePath(),
                    notification.getResourcePath(), "FILE", adminUser, resourceCache);

            GenericResource destinationResource = resourceList.get(resourceList.size() - 1);

            logger.info("Submitting resources to workflow manager");
            this.workflowServiceConnector.invokeWorkflow(notification.getAuthToken(), adminUser,
                    notification.getTenantId(), resourceIDsToProcess, sourceSP.getSshStoragePreference().getStoragePreferenceId(),
                    destinationResource.getResourceId(), destSP.getSshStoragePreference().getStoragePreferenceId());


            this.notificationClient.get().registerNotificationStatus(NotificationStatusRegisterRequest.newBuilder()
                    .setStatus(NotificationStatus.newBuilder()
                            .setStatusId(UUID.randomUUID().toString())
                            .setNotificationId(notification.getNotificationId())
                            .setStatus(NotificationStatus.StatusType.DISPATCHED_TO_WORFLOW_ENGING)
                            .setDescription("Notification successfully processed at the orchestrator. " +
                                    "Sending to workflow manager")
                            .setPublishedTime(System.currentTimeMillis())
                            .build()).build());

            logger.info("Completed processing path {}", notification.getResourcePath());

        } catch (Exception e) {
            logger.error("Failed to process event for resource path {}", notification.getResourcePath(), e);
            this.notificationClient.get().registerNotificationStatus(NotificationStatusRegisterRequest.newBuilder()
                    .setStatus(NotificationStatus.newBuilder()
                            .setStatusId(UUID.randomUUID().toString())
                            .setNotificationId(notification.getNotificationId())
                            .setStatus(NotificationStatus.StatusType.ERRORED)
                            .setDescription("Notification failed due to : " + e.getMessage())
                            .setPublishedTime(System.currentTimeMillis())
                            .build()).build());
        } finally {
            this.eventCache.remove(notification.getResourcePath() + ":" + notification.getHostName());
        }
    }

    private void scanResourceForChildResources(GenericResource resourceObj, AuthToken mftAuth, AnyStoragePreference sourceSP,
                                               String sourceStorageId, String sourceHostName, String adminUser,
                                               List<String> resourceIDsToProcess, Map<String, GenericResource> resourceCache,
                                               int scanDepth)
            throws Exception {

        FetchResourceMetadataRequest.Builder resourceMetadataReq = FetchResourceMetadataRequest.newBuilder()
                .setMftAuthorizationToken(mftAuth)
                .setResourceId(resourceObj.getResourceId());

        switch (sourceSP.getStorageCase()) {
            case SSH_STORAGE_PREFERENCE:
                resourceMetadataReq.setResourceType("SCP");
                resourceMetadataReq.setResourceToken(sourceSP.getSshStoragePreference().getStoragePreferenceId());
                break;
            case S3_STORAGE_PREFERENCE:
                resourceMetadataReq.setResourceType("S3");
                resourceMetadataReq.setResourceToken(sourceSP.getS3StoragePreference().getStoragePreferenceId());
                break;
        }

        DirectoryMetadataResponse directoryResourceMetadata;

        try (MFTApiClient mftApiClient = new MFTApiClient(
                this.configuration.getOutboundEventProcessor().getMftHost(),
                this.configuration.getOutboundEventProcessor().getMftPort())) {
            MFTApiServiceGrpc.MFTApiServiceBlockingStub mftClientStub = mftApiClient.get();
            directoryResourceMetadata = mftClientStub.getDirectoryResourceMetadata(resourceMetadataReq.build());

        } catch (Exception e) {
            logger.error("Failed to fetch dir metadata for resource {} with path {}",
                    resourceObj.getResourceId(), resourceObj.getResourcePath(), e);
            throw e;
        }

        for (FileMetadataResponse fileMetadata : directoryResourceMetadata.getFilesList()) {
            logger.info("Registering file {} for source storage {}", fileMetadata.getResourcePath(), sourceStorageId);
            List<GenericResource> resourceList = createResourceWithParentDirectories(sourceHostName, sourceStorageId, notification.getBasePath(),
                    fileMetadata.getResourcePath(), "FILE", adminUser, resourceCache);
            GenericResource fileResource = resourceList.get(resourceList.size() - 1);

            resourceIDsToProcess.add(fileResource.getResourceId());
        }

        for (DirectoryMetadataResponse directoryMetadata : directoryResourceMetadata.getDirectoriesList()) {
            logger.info("Registering directory {} for source storage {}", directoryMetadata.getResourcePath(), sourceStorageId);
            List<GenericResource> createResources = createResourceWithParentDirectories(sourceHostName, sourceStorageId, notification.getBasePath(),
                    directoryMetadata.getResourcePath(),
                    "COLLECTION", adminUser, resourceCache);
            GenericResource dirResource = createResources.get(createResources.size() - 1);

            if (scanDepth > 0) {
                // Scanning the directories recursively
                scanResourceForChildResources(dirResource, mftAuth, sourceSP, sourceStorageId, sourceHostName, adminUser,
                        resourceIDsToProcess, resourceCache, scanDepth - 1);
            }
        }
    }
}
