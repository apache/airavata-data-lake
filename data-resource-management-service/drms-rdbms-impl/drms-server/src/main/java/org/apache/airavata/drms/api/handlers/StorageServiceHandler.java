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
import org.apache.airavata.drms.api.persistance.model.Resource;
import org.apache.airavata.drms.api.persistance.model.ResourceProperty;
import org.apache.airavata.drms.api.persistance.repository.ResourcePropertyRepository;
import org.apache.airavata.drms.api.persistance.repository.ResourceRepository;
import org.apache.airavata.drms.api.persistance.repository.TransferMappingRepository;
import org.apache.airavata.drms.api.utils.CustosUtils;
import org.apache.airavata.drms.core.constants.SharingConstants;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.sharing.management.client.SharingManagementClient;
import org.apache.custos.sharing.service.Entity;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@GRpcService
public class StorageServiceHandler extends StorageServiceGrpc.StorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceHandler.class);


    @Autowired
    private CustosClientProvider custosClientProvider;

    @Autowired
    private ResourceRepository resourceRepository;


    @Autowired
    private ResourcePropertyRepository resourcePropertyRepository;


    @Autowired
    private TransferMappingRepository transferMappingRepository;


    @Override
    public void fetchStorage(StorageFetchRequest request, StreamObserver<StorageFetchResponse> responseObserver) {

        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            Map<String, Object> userProps = new HashMap<>();
            userProps.put("username", callUser.getUsername());
            userProps.put("tenantId", callUser.getTenantId());
            userProps.put("storageId", request.getStorageId());

            String storageId = request.getStorageId();

            boolean access = CustosUtils.userHasAccess(custosClientProvider, callUser.getTenantId(),
                    callUser.getUsername(), request.getStorageId(),
                    new String[]{SharingConstants.PERMISSION_TYPE_VIEWER,
                            SharingConstants.PERMISSION_TYPE_EDITOR, SharingConstants.PERMISSION_TYPE_OWNER});

            if (access) {
                try (SharingManagementClient sharingManagementClient = custosClientProvider.getSharingManagementClient()) {

                    Entity sharedEntity = Entity
                            .newBuilder()
                            .setId(storageId)
                            .build();

                    Entity entity = sharingManagementClient
                            .getEntity(callUser.getTenantId(), sharedEntity);

                    Optional<Resource> resourceOptional = resourceRepository.findById(storageId);
                    if (resourceOptional.isPresent()) {

                        AnyStorage anyStorage = StorageMapper.map(resourceOptional.get());

                        StorageFetchResponse storageFetchResponse = StorageFetchResponse
                                .newBuilder()
                                .setStorage(anyStorage)
                                .build();

                        responseObserver.onNext(storageFetchResponse);
                        responseObserver.onCompleted();
                        return;

                    }

                }
            }

        } catch (Exception exception) {

        }
    }

    @Override
    public void createStorage(StorageCreateRequest request, StreamObserver<StorageCreateResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            AnyStorage storage = request.getStorage();

            Resource resource = StorageMapper.map(storage, callUser);

            CustosUtils.
                    mergeStorageEntity(custosClientProvider, callUser.getTenantId(), resource.getId(), callUser.getUsername());

            resourceRepository.save(resource);

            StorageCreateResponse response = StorageCreateResponse
                    .newBuilder()
                    .setStorage(storage)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;


        } catch (Exception ex) {
            String msg = "Errored while creating storage; Message: {}" + ex.getMessage();
            logger.error(msg, ex);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void updateStorage(StorageUpdateRequest request, StreamObserver<StorageUpdateResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            AnyStorage storage = request.getStorage();

            Resource resource = StorageMapper.map(storage, callUser);


            boolean access = CustosUtils.userHasAccess(custosClientProvider, callUser.getTenantId(),
                    callUser.getUsername(), resource.getId(),
                    new String[]{SharingConstants.PERMISSION_TYPE_VIEWER,
                            SharingConstants.PERMISSION_TYPE_EDITOR, SharingConstants.PERMISSION_TYPE_OWNER});

            if (access) {
                Optional<Resource> optionalResource = resourceRepository.findById(resource.getId());


                if (optionalResource.isPresent()) {

                    Resource resource1 = optionalResource.get();

                    Set<ResourceProperty> resourceProperties = resource1.getResourceProperty();

                    resourceProperties.forEach(res -> {
                        resourcePropertyRepository.delete(res);
                    });

                    resourceRepository.save(resource);

                    StorageUpdateResponse response = StorageUpdateResponse.newBuilder().setStorage(storage).build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    return;
                }
            }

            //TODO:Error
            StorageUpdateResponse response = StorageUpdateResponse.newBuilder().setStorage(storage).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Errored while updating storage; Message: {}" + ex.getMessage();
            logger.error(msg, ex);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void deleteStorage(StorageDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            String id = request.getStorageId();

            boolean access = CustosUtils.userHasAccess(custosClientProvider, callUser.getTenantId(),
                    callUser.getUsername(), id,
                    new String[]{SharingConstants.PERMISSION_TYPE_VIEWER,
                            SharingConstants.PERMISSION_TYPE_EDITOR, SharingConstants.PERMISSION_TYPE_OWNER});
            if (access) {

                CustosUtils.deleteStorageEntity(custosClientProvider, callUser.getTenantId(), request.getStorageId());

                resourceRepository.deleteById(id);
            }

            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Errored while updating storage; Message: {}" + ex.getMessage();
            logger.error(msg, ex);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void addStorageMetadata(AddStorageMetadataRequest request, StreamObserver<Empty> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            String storageId = request.getStorageId();
            String key = request.getKey();
            String value = request.getValue();

            boolean access = CustosUtils.userHasAccess(custosClientProvider, callUser.getTenantId(),
                    callUser.getUsername(), storageId,
                    new String[]{SharingConstants.PERMISSION_TYPE_VIEWER,
                            SharingConstants.PERMISSION_TYPE_EDITOR, SharingConstants.PERMISSION_TYPE_OWNER});

            if (access) {

                Optional<Resource> resource = resourceRepository.findById(storageId);

                if (resource.isPresent()) {

                    Resource res = resource.get();

                    Set<ResourceProperty> resourcePropertySet = resource.get().getResourceProperty();
                    resourcePropertySet.add(new ResourceProperty(key, value, res));

                    res.setResourceProperty(resourcePropertySet);

                    resourceRepository.save(res);

                    responseObserver.onNext(Empty.newBuilder().build());
                    responseObserver.onCompleted();
                    return;
                }
            }
            //TODO:Error
        } catch (Exception ex) {
            String msg = "Errored while updating storage metadata; Message: {}" + ex.getMessage();
            logger.error(msg, ex);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void searchStorage(StorageSearchRequest request, StreamObserver<StorageSearchResponse> responseObserver) {
        AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

        Map<String, Object> userProps = new HashMap<>();
        userProps.put("username", callUser.getUsername());
        userProps.put("tenantId", callUser.getTenantId());
        userProps.put("scope", TransferScope.GLOBAL.name());


        responseObserver.onError(Status.UNIMPLEMENTED.asRuntimeException());
    }


    @Override
    public void createTransferMapping(CreateTransferMappingRequest request, StreamObserver<CreateTransferMappingResponse> responseObserver) {
        try {
            AuthenticatedUser authenticatedUser = request.getAuthToken().getAuthenticatedUser();
            AnyStorage sourceStorage = request.getTransferMapping().getSourceStorage();
            AnyStorage destinationStorage = request.getTransferMapping().getDestinationStorage();
            TransferScope scope = request.getTransferMapping().getTransferScope();

            Optional<Resource> optionalSource = Optional.empty();
            Optional<Resource> optionalDst = Optional.empty();


            if (sourceStorage.isInitialized()) {
                optionalSource = resourceRepository.findById(getStorageId(sourceStorage));
            }

            if (destinationStorage.isInitialized()) {
                optionalDst = resourceRepository.findById(getStorageId(destinationStorage));
            }

            if (optionalSource.isEmpty() || optionalDst.isEmpty()) {
               responseObserver.onError(Status.NOT_FOUND.
                       withDescription("source or destination storages are not found").asRuntimeException());
               return;
            }

           Optional<org.apache.airavata.drms.api.persistance.model.TransferMapping> transferMappingOp =
                   transferMappingRepository.findTransferMappingBySourceIdAndDestinationId(
                    optionalSource.get().getId(),optionalDst.get().getId());

            if (transferMappingOp.isPresent()){
                responseObserver.onError(Status.ALREADY_EXISTS.
                        withDescription("source or destination storages are not found").asRuntimeException());
                return;
            }

            org.apache.airavata.drms.api.persistance.model.TransferMapping transferMapping = new
                    org.apache.airavata.drms.api.persistance.model.TransferMapping();

            if (optionalSource.isPresent()) {

                Set<org.apache.airavata.drms.api.persistance.model.TransferMapping> transferMappings =
                        optionalSource.get().getSourceTransferMapping();
                if (transferMappings != null) {
                    transferMappings.add(transferMapping);
                } else {
                    transferMappings = new HashSet<>();
                    transferMappings.add(transferMapping);
                }
                transferMapping.setSource(optionalSource.get());
                optionalSource.get().setSourceTransferMapping(transferMappings);
            }

            if (optionalDst.isPresent()) {
                Set<org.apache.airavata.drms.api.persistance.model.TransferMapping> transferMappings =
                        optionalDst.get().getDestinationTransferMapping();
                if (transferMappings != null) {
                    transferMappings.add(transferMapping);
                } else {
                    transferMappings = new HashSet<>();
                    transferMappings.add(transferMapping);
                }
                transferMapping.setDestination(optionalDst.get());
                optionalDst.get().setDestinationTransferMapping(transferMappings);
            }

            transferMapping.setScope(scope.name());
            transferMapping.setOwnerId(authenticatedUser.getUsername());

            transferMappingRepository.save(transferMapping);

            CreateTransferMappingResponse createTransferMappingResponse = CreateTransferMappingResponse
                    .newBuilder()
                    .setTransferMapping(request.getTransferMapping())
                    .build();
            responseObserver.onNext(createTransferMappingResponse);
            responseObserver.onCompleted();

        } catch (Exception e) {
            String msg = "Errored while creating transfer mapping; Message:" + e.getMessage();
            logger.error("Errored while creating transfer mapping; Message: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }

    }

    @Override
    public void getTransferMappings(FindTransferMappingsRequest request, StreamObserver<FindTransferMappingsResponse> responseObserver) {
        try {
            List<TransferMapping> transferMappings = new ArrayList<>();
            AuthenticatedUser authenticatedUser = request.getAuthToken().getAuthenticatedUser();
            Map<String, Object> properties = new HashMap<>();


            List<org.apache.airavata.drms.api.persistance.model.TransferMapping> transferMappingList =
                    transferMappingRepository.findAll();

            List<TransferMapping> mappingList = transferMappingList.stream().map(trans -> {

                Resource source = trans.getSource();
                Resource dst = trans.getDestination();
                String scope = trans.getScope();

                TransferMapping.Builder transferMapping = TransferMapping.newBuilder();

                try {
                    if (source != null) {

                        transferMapping.setSourceStorage(StorageMapper.map(source));
                    }

                    if (dst != null) {
                        StorageMapper.map(dst);
                        transferMapping.setDestinationStorage(StorageMapper.map(dst));
                    }

                    transferMapping.setTransferScope(TransferScope.valueOf(scope));
                    return transferMapping.build();

                } catch (Exception exception) {
                    logger.error("Errored while fetching transfer mappings, Message: {}", exception.getMessage(), exception);

                }
                return TransferMapping.newBuilder().build();
            }).collect(Collectors.toList());


            FindTransferMappingsResponse findTransferMappingsResponse = FindTransferMappingsResponse
                    .newBuilder()
                    .addAllMappings(mappingList)
                    .build();
            responseObserver.onNext(findTransferMappingsResponse);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Errored while fetching transfer mappings, Message:" + ex.getMessage();
            logger.error("Errored while fetching transfer mappings, Message: {}", ex.getMessage(), ex);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void deleteTransferMappings(DeleteTransferMappingRequest request, StreamObserver<Empty> responseObserver) {
        try {
            AuthenticatedUser authenticatedUser = request.getAuthToken().getAuthenticatedUser();
            String transferMappingId = request.getId();
            Map<String, Object> properties = new HashMap<>();

            transferMappingRepository.deleteById(transferMappingId);

            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Errored while delete transfer mappings, Message:" + ex.getMessage();
            logger.error("Errored while delete transfer mappings, Message: {}", ex.getMessage(), ex);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }


    private String getStorageId(AnyStorage storage) {
        if (storage.getStorageCase()
                .equals(AnyStorage.StorageCase.S3_STORAGE)) {
            return storage.getS3Storage().getStorageId();
        } else {
            return storage.getSshStorage().getStorageId();
        }
    }

}
