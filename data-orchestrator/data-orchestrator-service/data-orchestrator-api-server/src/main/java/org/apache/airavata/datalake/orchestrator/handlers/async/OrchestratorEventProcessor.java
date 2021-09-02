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

import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.drms.storage.AnyStoragePreference;
import org.apache.airavata.datalake.drms.storage.TransferMapping;
import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.Utils;
import org.apache.airavata.datalake.orchestrator.connectors.DRMSConnector;
import org.apache.airavata.datalake.orchestrator.connectors.WorkflowServiceConnector;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;
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

    private NotificationEvent notificationEvent;

    private DRMSConnector drmsConnector;
    private Configuration configuration;
    private WorkflowServiceConnector workflowServiceConnector;
    private final Set<String> eventCache;

    public OrchestratorEventProcessor(Configuration configuration, NotificationEvent notificationEvent,
                                      Set<String> eventCache) throws Exception {
        this.notificationEvent = notificationEvent;
        this.eventCache = eventCache;
        this.drmsConnector = new DRMSConnector(configuration);
        this.workflowServiceConnector = new WorkflowServiceConnector(configuration);
        this.configuration = configuration;
    }

    private List<GenericResource> createResourceRecursively(String storageId, String basePath,
                                                            String resourcePath, String resourceType, String user)
            throws Exception {

        List<GenericResource> resourceList = new ArrayList<>();

        String parentType = "Storage";

        String[] splitted = resourcePath.substring(basePath.length()).split("/");

        String currentPath = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        String parentId = storageId;
        for (int i = 0; i < splitted.length - 1; i++) {
            String resourceName = splitted[i];
            currentPath = currentPath + "/" + resourceName;
            String resourceId = Utils.getId(storageId + ":" + currentPath);
            Optional<GenericResource> optionalGenericResource =
                    this.drmsConnector.createResource(notificationEvent.getAuthToken(),
                            notificationEvent.getTenantId(),
                            resourceId, resourceName, currentPath, parentId, "COLLECTION", parentType, user);
            if (optionalGenericResource.isPresent()) {
                parentId = optionalGenericResource.get().getResourceId();
                parentType = "COLLECTION";
                resourceList.add(optionalGenericResource.get());
            } else {
                logger.error("Could not create a resource for path {}", currentPath);
                throw new Exception("Could not create a resource for path " + currentPath);
            }
        }

        currentPath = currentPath + "/" + splitted[splitted.length - 1];

        Optional<GenericResource> optionalGenericResource =
                this.drmsConnector.createResource(notificationEvent.getAuthToken(),
                        notificationEvent.getTenantId(),
                        Utils.getId(storageId + ":" + currentPath),
                        splitted[splitted.length - 1], currentPath,
                        parentId, resourceType, parentType, user);

        if (optionalGenericResource.isPresent()) {
            resourceList.add(optionalGenericResource.get());
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
            this.drmsConnector.shareWithUser(notificationEvent.getAuthToken(), notificationEvent.getTenantId(),
                    admin, user, resource.getResourceId(), permission);
        }
    }

    private void shareResourcesWithGroups(List<GenericResource> resourceList, String admin, String group, String permission) throws Exception {
        for (GenericResource resource : resourceList) {
            logger.info("Sharing resource {} with path {} with group {}",
                    resource.getResourceId(), resource.getResourcePath(), group);
            this.drmsConnector.shareWithGroup(notificationEvent.getAuthToken(), notificationEvent.getTenantId(),
                    admin, group, resource.getResourceId(), permission);
        }
    }

    @Override
    public void run() {
        logger.info("Processing resource path {} on storage {}", notificationEvent.getResourcePath(),
                notificationEvent.getBasePath());

        try {

            if (!"FOLDER".equals(notificationEvent.getResourceType())) {
                logger.error("Resource {} should be a Folder type but got {}",
                        notificationEvent.getResourcePath(),
                        notificationEvent.getResourceType());
                logger.error("Resource should be a Folder type");
            }
            String removeBasePath = notificationEvent.getResourcePath().substring(notificationEvent.getBasePath().length());
            String[] splitted = removeBasePath.split("/");

            String adminUser = splitted[0];
            String owner = splitted[1].split("_")[0];

            Map<String, String> ownerRules = new HashMap<>();
            ownerRules.put(adminUser, "VIEWER");
            ownerRules.put(splitted[1], "OWNER");

            Optional<TransferMapping> optionalTransferMapping = drmsConnector.getActiveTransferMapping(
                    notificationEvent.getAuthToken(),
                    notificationEvent.getTenantId(), adminUser,
                    notificationEvent.getHostName());

            if (optionalTransferMapping.isEmpty()) {
                logger.error("Could not find a transfer mapping for user {} and host {}", adminUser, notificationEvent.getHostName());
                throw new Exception("Could not find a transfer mapping");
            }

            TransferMapping transferMapping = optionalTransferMapping.get();

            String sourceStorageId = transferMapping.getSourceStorage().getSshStorage().getStorageId();
            String destinationStorageId = transferMapping.getDestinationStorage().getSshStorage().getStorageId();

            // Creating parent resource

            List<GenericResource> resourceList = createResourceRecursively(sourceStorageId,
                    notificationEvent.getBasePath(),
                    notificationEvent.getResourcePath(),
                    "COLLECTION", adminUser);

            shareResourcesWithUsers(Collections.singletonList(resourceList.get(resourceList.size() - 1)),
                    adminUser, owner, "VIEWER");

            shareResourcesWithGroups(Collections.singletonList(resourceList.get(resourceList.size() - 1)), adminUser,
                    configuration.getTenantConfigs().getAdminGroup(),
                    "EDITOR");

//            shareResourcesWithGroups(Collections.singletonList(resourceList.get(resourceList.size() - 1)), adminUser,
//                    configuration.getTenantConfigs().getUserGroup(),
//                    "VIEWER");

            GenericResource resourceObj = resourceList.get(resourceList.size() - 1);

            Optional<AnyStoragePreference> sourceSPOp = this.drmsConnector.getStoragePreference(
                    notificationEvent.getAuthToken(), adminUser,
                    notificationEvent.getTenantId(), sourceStorageId);

            if (sourceSPOp.isEmpty()) {
                logger.error("No storage preference found for source storage {} and user {}", sourceStorageId, adminUser);
                throw new Exception("No storage preference found for source storage");
            }

            Optional<AnyStoragePreference> destSPOp = this.drmsConnector.getStoragePreference(
                    notificationEvent.getAuthToken(), adminUser,
                    notificationEvent.getTenantId(), destinationStorageId);

            if (destSPOp.isEmpty()) {
                logger.error("No storage preference found for destination storage {} and user {}", sourceStorageId, adminUser);
                throw new Exception("No storage preference found for destination storage");
            }

            AnyStoragePreference sourceSP = sourceSPOp.get();
            AnyStoragePreference destSP = destSPOp.get();

            String decodedAuth = new String(Base64.getDecoder().decode(notificationEvent.getAuthToken()));
            String[] authParts = decodedAuth.split(":");

            if (authParts.length != 2) {
                throw new Exception("Could not decode auth token to work with MFT");
            }

            DelegateAuth delegateAuth = DelegateAuth.newBuilder()
                    .setUserId(adminUser)
                    .setClientId(authParts[0])
                    .setClientSecret(authParts[1])
                    .putProperties("TENANT_ID", notificationEvent.getTenantId()).build();

            AuthToken mftAuth = AuthToken.newBuilder().setDelegateAuth(delegateAuth).build();

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

            // Fetching file list for parent resource

            DirectoryMetadataResponse directoryResourceMetadata;

            try (MFTApiClient mftApiClient = new MFTApiClient(
                    this.configuration.getOutboundEventProcessor().getMftHost(),
                    this.configuration.getOutboundEventProcessor().getMftPort())) {
                MFTApiServiceGrpc.MFTApiServiceBlockingStub mftClientStub = mftApiClient.get();
                directoryResourceMetadata = mftClientStub.getDirectoryResourceMetadata(resourceMetadataReq.build());
            }

            List<String> resourceIDsToProcess = new ArrayList<>();
            for (FileMetadataResponse fileMetadata : directoryResourceMetadata.getFilesList()) {
                logger.info("Registering file {} for source storage {}", fileMetadata.getResourcePath(), sourceStorageId);
                resourceList = createResourceRecursively(sourceStorageId, notificationEvent.getBasePath(),
                        fileMetadata.getResourcePath(), "FILE", adminUser);
                GenericResource fileResource = resourceList.get(resourceList.size() - 1);

                resourceIDsToProcess.add(fileResource.getResourceId());
            }

            for (DirectoryMetadataResponse directoryMetadata : directoryResourceMetadata.getDirectoriesList()) {
                logger.info("Registering directory {} for source storage {}", directoryMetadata.getResourcePath(), sourceStorageId);
                createResourceRecursively(sourceStorageId, notificationEvent.getBasePath(),
                        directoryMetadata.getResourcePath(),
                        "COLLECTION", adminUser);
                // TODO scan directories
            }

            logger.info("Creating destination zip resource for directory {}", notificationEvent.getResourcePath());
            resourceList = createResourceRecursively(destinationStorageId, notificationEvent.getBasePath(),
                    notificationEvent.getResourcePath(), "FILE", adminUser);

            GenericResource destinationResource = resourceList.get(resourceList.size() - 1);

            System.out.println(destinationResource);

            logger.info("Submitting resources to workflow manager");
            this.workflowServiceConnector.invokeWorkflow(notificationEvent.getAuthToken(), adminUser,
                    notificationEvent.getTenantId(), resourceIDsToProcess, sourceSP.getSshStoragePreference().getStoragePreferenceId(),
                    destinationResource.getResourceId(), destSP.getSshStoragePreference().getStoragePreferenceId());


            logger.info("Completed processing path {}", notificationEvent.getResourcePath());

        } catch (Exception e) {
            logger.error("Failed to process event for resource path {}", notificationEvent.getResourcePath(), e);
        } finally {
            this.eventCache.remove(notificationEvent.getResourcePath() + ":" + notificationEvent.getHostName());
        }
    }
}
