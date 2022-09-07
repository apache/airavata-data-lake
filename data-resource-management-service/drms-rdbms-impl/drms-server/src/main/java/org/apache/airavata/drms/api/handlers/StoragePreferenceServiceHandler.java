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
package org.apache.airavata.drms.api.handlers;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.airavata.drms.api.persistance.mapper.StorageMapper;
import org.apache.airavata.drms.api.persistance.mapper.StoragePreferenceMapper;
import org.apache.airavata.drms.api.persistance.model.Resource;
import org.apache.airavata.drms.api.persistance.model.ResourceProperty;
import org.apache.airavata.drms.api.persistance.repository.ResourcePropertyRepository;
import org.apache.airavata.drms.api.persistance.repository.ResourceRepository;
import org.apache.airavata.drms.api.utils.CustosUtils;
import org.apache.airavata.drms.core.constants.SharingConstants;
import org.apache.airavata.drms.core.constants.StoragePreferenceConstants;
import org.apache.airavata.drms.core.serializer.AnyStoragePreferenceSerializer;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.sharing.management.client.SharingManagementClient;
import org.apache.custos.sharing.service.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@GRpcService
public class StoragePreferenceServiceHandler extends StoragePreferenceServiceGrpc.StoragePreferenceServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StoragePreferenceServiceHandler.class);

    @Autowired
    private CustosClientProvider custosClientProvider;


    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ResourcePropertyRepository resourcePropertyRepository;


    public StoragePreferenceServiceHandler() {

    }


    @Override
    public void fetchStoragePreference(StoragePreferenceFetchRequest request, StreamObserver<StoragePreferenceFetchResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            String storagePreferenceId = request.getStoragePreferenceId();

            CustosUtils.userHasAccess(custosClientProvider, callUser.getTenantId(), callUser.getUsername(),
                    storagePreferenceId, new String[]{SharingConstants.PERMISSION_TYPE_VIEWER, SharingConstants.PERMISSION_TYPE_EDITOR, SharingConstants.PERMISSION_TYPE_OWNER});

            Optional<Resource> optionalResource = resourceRepository.findById(storagePreferenceId);

            if (optionalResource.isPresent()) {


                String storageId = optionalResource.get().getParentResourceId();

                Optional<Resource> optionalStorage = resourceRepository.findById(storageId);

                AnyStorage storage = StorageMapper.map(optionalStorage.get());

                AnyStoragePreference anyStoragePreference = StoragePreferenceMapper.map(optionalResource.get(), storage);
                StoragePreferenceFetchResponse response = StoragePreferenceFetchResponse
                        .newBuilder()
                        .setStoragePreference(anyStoragePreference)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;

            }
            //TODO:Error

        } catch (Exception e) {
            String msg = "Errored while searching storage preferences; Message:" + e.getMessage();
            logger.error("Errored while searching storage preferences; Message: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void createStoragePreference(StoragePreferenceCreateRequest request, StreamObserver<StoragePreferenceCreateResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            AnyStoragePreference storage = request.getStoragePreference();
            Map<String, Object> serializedMap = AnyStoragePreferenceSerializer.serializeToMap(storage);
            String storagePreferenceId = (String) serializedMap.get("storagePreferenceId");
            serializedMap.remove("storage");
            String storageId = null;

            if (resourceRepository.findById(storagePreferenceId).isPresent()) {
                responseObserver.onError(Status.ALREADY_EXISTS.asRuntimeException());
                return;
            }


            Resource resource = new Resource();
            resource.setTenantId(callUser.getTenantId());
            resource.setId(storagePreferenceId);
            resource.setResourceType(StoragePreferenceConstants.STORAGE_PREFERENCE_LABEL);
            if (storage.getStorageCase().equals(AnyStoragePreference.StorageCase.S3_STORAGE_PREFERENCE)) {
                storageId = storage.getS3StoragePreference().getStorage().getStorageId();
                serializedMap.put(StoragePreferenceConstants.STORAGE_PREFERENCE_TYPE_LABEL,
                        StoragePreferenceConstants.S3_STORAGE_PREFERENCE_TYPE_LABEL);
                resource.setParentResourceId(storageId);


            } else if (storage.getStorageCase()
                    .equals(AnyStoragePreference.StorageCase.SSH_STORAGE_PREFERENCE)) {

                storageId = storage.getSshStoragePreference().getStorage().getStorageId();
                if (request.getStoragePreference().getSdaStoragePreference().isInitialized() &&
                        !request.getStoragePreference().getSdaStoragePreference().getStoragePreferenceId().isEmpty()) {
                }

                serializedMap.put(StoragePreferenceConstants.STORAGE_PREFERENCE_TYPE_LABEL,
                        StoragePreferenceConstants.SSH_STORAGE_PREFERENCE_TYPE_LABEL);
                resource.setParentResourceId(storageId);
            } else if (storage.getStorageCase()
                    .equals(AnyStoragePreference.StorageCase.SDA_STORAGE_PREFERENCE)) {

                storageId = storage.getSdaStoragePreference().getStorage().getStorageId();
                serializedMap.put(StoragePreferenceConstants.STORAGE_PREFERENCE_TYPE_LABEL,
                        StoragePreferenceConstants.SDA_STORAGE_PREFERENCE_TYPE_LABEL);
                resource.setParentResourceId(storageId);
            }

            if (storageId == null || resourceRepository.findById(storageId).isEmpty()) {

                responseObserver.onError(Status.NOT_FOUND.withDescription("Storage not found").asRuntimeException());
                return;
            }

            CustosUtils.
                    mergeStoragePreferenceEntity(custosClientProvider, callUser.getTenantId(),
                            storagePreferenceId, storageId, callUser.getUsername());
            Set<ResourceProperty> resourcePropertySet = new HashSet<>();

            serializedMap.forEach((key, value) -> {
                resourcePropertySet.add(new ResourceProperty(key, value.toString(), resource));
            });
            resource.setResourceProperty(resourcePropertySet);

            resourceRepository.save(resource);

            StoragePreferenceCreateResponse response = StoragePreferenceCreateResponse
                    .newBuilder().setStoragePreference(storage).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Errored while creating storage; Message: {}" + ex.getMessage();
            logger.error(msg, ex);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void updateStoragePreference(StoragePreferenceUpdateRequest request, StreamObserver<StoragePreferenceUpdateResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            AnyStoragePreference storage = request.getStoragePreference();
            Map<String, Object> serializedMap = AnyStoragePreferenceSerializer.serializeToMap(storage);
            String storagePreferenceId = (String) serializedMap.get("storagePreferenceId");
            serializedMap.remove("storage");
            String storageId = null;


            boolean access = CustosUtils.
                    userHasAccess(custosClientProvider, callUser.getTenantId(), callUser.getUsername(),
                            storagePreferenceId, new String[]{SharingConstants.PERMISSION_TYPE_VIEWER,
                                    SharingConstants.PERMISSION_TYPE_EDITOR, SharingConstants.PERMISSION_TYPE_OWNER});

            if (access) {
                if (storage.getStorageCase().name()
                        .equals(AnyStoragePreference.StorageCase.S3_STORAGE_PREFERENCE)) {
                    storageId = storage.getS3StoragePreference().getStorage().getStorageId();
                } else if (storage.getStorageCase()
                        .equals(AnyStoragePreference.StorageCase.SSH_STORAGE_PREFERENCE)) {

                    storageId = storage.getSshStoragePreference().getStorage().getStorageId();
                } else if (storage.getStorageCase()
                        .equals(AnyStoragePreference.StorageCase.SDA_STORAGE_PREFERENCE)) {

                    storageId = storage.getSdaStoragePreference().getStorage().getStorageId();
                }


                if (storageId != null) {
                    CustosUtils.
                            mergeStoragePreferenceEntity(custosClientProvider, callUser.getTenantId(),
                                    storagePreferenceId, storageId, callUser.getUsername());
                    Optional<Resource> resourceOptional = resourceRepository.findById(storageId);
                    Optional<Resource> storagePrefOptional = resourceRepository.findById(storagePreferenceId);


                    if (resourceOptional.isPresent() && storagePrefOptional.isPresent()) {


                        Resource resource = storagePrefOptional.get();

                        resource.getResourceProperty().forEach(property -> {

                            resourcePropertyRepository.delete(property);

                        });

                        Set<ResourceProperty> resourcePropertySet = new HashSet<>();

                        serializedMap.forEach((key, value) -> {
                            resourcePropertySet.add(new ResourceProperty(key, value.toString(), resource));
                        });

                        resourceRepository.save(resource);
                    }
                }
                //TODO:Error
            }
            StoragePreferenceUpdateResponse response = StoragePreferenceUpdateResponse.newBuilder().setStoragePreference(storage).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Errored while updating storage; Message: {}" + ex.getMessage();
            logger.error(msg, ex);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void deletePreferenceStorage(StoragePreferenceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            String id = request.getStoragePreferenceId();

            boolean accessEditor = CustosUtils.userHasAccess(custosClientProvider, callUser.getTenantId(),
                    callUser.getUsername(), id, new String[]{SharingConstants.PERMISSION_TYPE_VIEWER,
                            SharingConstants.PERMISSION_TYPE_EDITOR, SharingConstants.PERMISSION_TYPE_OWNER});
            if (accessEditor) {
                resourceRepository.deleteById(id);

                responseObserver.onNext(Empty.newBuilder().build());
                responseObserver.onCompleted();
                return;
            } else {
                responseObserver.onError(Status.UNAUTHENTICATED.asRuntimeException());
            }
        } catch (Exception ex) {
            String msg = "Errored while updating storage; Message: {}" + ex.getMessage();
            logger.error(msg, ex);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void searchStoragePreference(StoragePreferenceSearchRequest request, StreamObserver<StoragePreferenceSearchResponse> responseObserver) {
        try {

            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            List<AnyStoragePreference> anyStoragePreferenceList = new ArrayList<>();

            try (SharingManagementClient sharingManagementClient = custosClientProvider.getSharingManagementClient()) {

                SearchCriteria searchCriteria = SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.ENTITY_TYPE_ID)
                        .setCondition(SearchCondition.EQUAL)
                        .setValue(StoragePreferenceConstants.STORAGE_PREFERENCE_LABEL).build();

                SearchRequest searchRequest = SearchRequest.newBuilder().setOwnerId(callUser.getUsername())
                        .addSearchCriteria(searchCriteria).build();
                Entities entities = sharingManagementClient.searchEntities(callUser.getTenantId(),
                        searchRequest);

                List<Entity> entityList = entities.getEntityArrayList();


                entityList.forEach(entity -> {

                    Optional<Resource> resource = resourceRepository.findById(entity.getId());
                    if (resource.isPresent()) {

                        String storageId = resource.get().getParentResourceId();

                        if (request.getQueriesList().size() > 0) {
                            request.getQueriesList().forEach(query -> {
                                if (query.getField().equals("storageId") && query.getValue().equals(storageId)) {
                                    Optional<Resource> storageOptional = resourceRepository.findById(storageId);

                                    if (storageOptional.isPresent()) {
                                        try {
                                            AnyStorage anyStorage = StorageMapper.map(storageOptional.get());
                                            AnyStoragePreference anyStoragePreference = StoragePreferenceMapper.map(resource.get(), anyStorage);
                                            anyStoragePreferenceList.add(anyStoragePreference);
                                        } catch (Exception exception) {
                                            logger.error(" Mapping error ", exception);
                                        }
                                    }
                                }
                            });
                        } else {
                            Optional<Resource> storageOptional = resourceRepository.findById(storageId);

                            if (storageOptional.isPresent()) {
                                try {
                                    AnyStorage anyStorage = StorageMapper.map(storageOptional.get());
                                    AnyStoragePreference anyStoragePreference = StoragePreferenceMapper.map(resource.get(), anyStorage);
                                    anyStoragePreferenceList.add(anyStoragePreference);
                                } catch (Exception exception) {
                                    logger.error(" Mapping error ", exception);
                                }
                            }
                        }
                    }
                });

            }

            StoragePreferenceSearchResponse.Builder builder = StoragePreferenceSearchResponse.newBuilder();
            builder.addAllStoragesPreference(anyStoragePreferenceList);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            String msg = "Errored while searching storage preferences; Message:" + e.getMessage();
            logger.error("Errored while searching storage preferences; Message: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }


}
