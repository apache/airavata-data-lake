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
import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.airavata.drms.api.utils.CustosUtils;
import org.apache.airavata.drms.api.utils.Utils;
import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.airavata.drms.core.constants.StorageConstants;
import org.apache.airavata.drms.core.deserializer.AnyStorageDeserializer;
import org.apache.airavata.drms.core.deserializer.TransferMappingDeserializer;
import org.apache.airavata.drms.core.serializer.AnyStorageSerializer;
import org.apache.custos.clients.CustosClientProvider;
import org.lognet.springboot.grpc.GRpcService;
import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GRpcService
public class StorageServiceHandler extends StorageServiceGrpc.StorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceHandler.class);

    @Autowired
    private Neo4JConnector neo4JConnector;

    @Autowired
    private CustosClientProvider custosClientProvider;


    @Override
    public void fetchStorage(StorageFetchRequest request, StreamObserver<StorageFetchResponse> responseObserver) {

        AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

        Map<String, Object> userProps = new HashMap<>();
        userProps.put("username", callUser.getUsername());
        userProps.put("tenantId", callUser.getTenantId());
        userProps.put("storageId", request.getStorageId());
        List<Record> records = this.neo4JConnector.searchNodes(userProps,
                " MATCH (u:User) where u.username = $username = $username AND u.tenantId = $tenantId" +
                        " OPTIONAL MATCH (u)<-[r2:SHARED_WITH]-(s:Storage) where s.storageId = $storageId AND s.tenantId = $tenantId" +
                        " OPTIONAL MATCH (ch:Group)-[CHILD_OF*]->(g:Group)<-[r3:MEMBER_OF]-(u)" +
                        " OPTIONAL MATCH (cs:Storage)-[SHARED_WITH]->(ch) where cs.storageId = $storageId AND s.tenantId = $tenantId" +
                        " OPTIONAL MATCH (ds:Storage)-[SHARED_WITH]->(g)where ds.storageId = $storageId AND s.tenantId = $tenantId" +
                        " return distinct s, cs, ds");
        if (!records.isEmpty()) {
            try {
                List<AnyStorage> storageList = AnyStorageDeserializer.deserializeList(records);
                StorageFetchResponse.Builder builder = StorageFetchResponse.newBuilder();
                builder.setStorage(storageList.get(0));
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
            } catch (Exception e) {
                String msg = "(Errored while searching storages; Message: {}, e.getMessage())";
                logger.error(msg, e);
                responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        } else {
            logger.error("Could not find a storage with id {}", request.getStorageId());
            responseObserver.onError(new Exception("Could not find a storage with id " + request.getStorageId()));
        }
    }

    @Override
    public void createStorage(StorageCreateRequest request, StreamObserver<StorageCreateResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            AnyStorage storage = request.getStorage();

            Map<String, Object> serializedMap = AnyStorageSerializer.serializeToMap(storage);
            String storageId = (String) serializedMap.get("storageId");

            CustosUtils.
                    mergeStorageEntity(custosClientProvider, callUser.getTenantId(), storageId, callUser.getUsername());
            this.neo4JConnector.mergeNode(serializedMap, StorageConstants.STORAGE_LABEL, callUser.getUsername(), storageId,
                    callUser.getTenantId());
            StorageCreateResponse response = StorageCreateResponse.newBuilder().setStorage(storage).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
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
            Map<String, Object> serializedMap = AnyStorageSerializer.serializeToMap(storage);
            String storageId = (String) serializedMap.get("storageId");
            this.neo4JConnector.mergeNode(serializedMap, StorageConstants.STORAGE_LABEL, callUser.getUsername(), storageId,
                    callUser.getTenantId());
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

            CustosUtils.deleteStorageEntity(custosClientProvider, callUser.getTenantId(), request.getStorageId());

            this.neo4JConnector.deleteNode(StorageConstants.STORAGE_LABEL, id, callUser.getTenantId());
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
            this.neo4JConnector.createMetadataNode(StorageConstants.STORAGE_LABEL, "storageId",
                    request.getStorageId(), callUser.getUsername(),
                    request.getKey(), request.getValue());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
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
        List<Record> records = this.neo4JConnector.searchNodes(userProps,
                " MATCH (u:User) where u.username = $username = $username AND u.tenantId = $tenantId" +
                        " OPTIONAL MATCH (u)<-[r2:SHARED_WITH]-(s:Storage)" +
                        " OPTIONAL MATCH (ch:Group)-[CHILD_OF *0..]->(g:Group)<-[r3:MEMBER_OF]-(u)" +
                        " OPTIONAL MATCH (cs:Storage)-[SHARED_WITH]->(ch) " +
                        " return distinct s, cs");
        try {
            List<AnyStorage> storageList = AnyStorageDeserializer.deserializeList(records);
            StorageSearchResponse.Builder builder = StorageSearchResponse.newBuilder();
            builder.addAllStorages(storageList);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            String msg = "(Errored while searching storages; Message: {}, e.getMessage())";
            logger.error(msg, e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }


    @Override
    public void createTransferMapping(CreateTransferMappingRequest request, StreamObserver<CreateTransferMappingResponse> responseObserver) {
        try {
            AuthenticatedUser authenticatedUser = request.getAuthToken().getAuthenticatedUser();
            AnyStorage sourceStorage = request.getTransferMapping().getSourceStorage();
            AnyStorage destinationStorage = request.getTransferMapping().getDestinationStorage();
            TransferScope scope = request.getTransferMapping().getTransferScope();
            Map<String, Object> properties = new HashMap<>();
            Map<String, Object> props = new HashMap<>();
            props.put("tenantId", authenticatedUser.getTenantId());
            props.put("owner", authenticatedUser.getUsername());
            properties.put("tenantId", authenticatedUser.getTenantId());
            properties.put("username", authenticatedUser.getUsername());
            properties.put("owner", authenticatedUser.getUsername());
            List<TransferMapping> transferMappings = new ArrayList<>();

            if (scope.equals(TransferScope.GLOBAL)) {
                props.put("scope", TransferScope.GLOBAL.name());
            } else {
                props.put("scope", TransferScope.USER.name());
            }

            if (sourceStorage.getSshStorage() != null && !sourceStorage.getSshStorage().getStorageId().isEmpty() &&
                    destinationStorage.getSshStorage() == null || destinationStorage.getSshStorage().getStorageId().isEmpty()) {
                String sourceId = sourceStorage.getSshStorage().getStorageId();
                properties.put("srcStorageId", sourceId);
                props.put("srcStorageId", sourceId);
                String messageId = authenticatedUser.getUsername() + "_" + sourceId;
                String entityId = Utils.getId(messageId);
                properties.put("props", props);
                properties.put("entityId", entityId);
                props.put("entityId", entityId);
                if (hasAccess(authenticatedUser.getUsername(), authenticatedUser.getTenantId(), sourceId)) {
                    String query = " Match (u:User), (srcSp:Storage) where " +
                            " u.username=$username AND u.tenantId=$tenantId AND " +
                            "srcSp.storageId=$srcStorageId AND " +
                            "srcSp.tenantId = $tenantId " +
                            " Merge (u)-[:HAS_TRANSFER_MAPPING]->(tm:TransferMapping{entityId:$entityId, tenantId:$tenantId, " +
                            "srcStorageId:$srcStorageId," +
                            "owner:$owner}) set tm += $props" +
                            " Merge (tm)<-[:TRANSFER_OUT]-(srcSp)" +
                            " return (tm)";
                    this.neo4JConnector.runTransactionalQuery(properties, query);

                    String searchQuery = " Match (srcStr:Storage)-[:TRANSFER_OUT]->(tm:TransferMapping)" +
                            " where " +
                            " tm.entityId=$entityId AND tm.tenantId=$tenantId return srcStr, tm";
                    List<Record> records = this.neo4JConnector.searchNodes(properties, searchQuery);
                    if (!records.isEmpty()) {
                        transferMappings = TransferMappingDeserializer.deserializeListExceptDestinationStorage(records);
                    }
                }

            } else {
                String sourceId = null;

                if (sourceStorage.getSshStorage() == null || sourceStorage.getSshStorage().getStorageId().isEmpty()) {
                    String query = " Match (srcStr:Storage)-[:TRANSFER_OUT]->(tm:TransferMapping) where tm.tenantId=$tenantId AND tm.scope='GLOBAL' " +
                            " AND srcStr.tenantId=$tenantId return srcStr, tm";
                    List<Record> sourceRecords = this.neo4JConnector.searchNodes(properties, query);
                    List<TransferMapping> sourceTransfers = TransferMappingDeserializer.deserializeListExceptDestinationStorage(sourceRecords);
                    if (sourceTransfers.isEmpty()) {
                        String msg = "Errored while creating transfer mapping; Message: Cannot find global source storage ";
                        logger.error("Errored while creating transfer mapping; Message: Cannot find global source storage ");
                        responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
                        return;
                    }
                    TransferMapping transferMapping = sourceTransfers.get(0);
                    sourceId = getStorageId(transferMapping.getSourceStorage());

                } else {
                    sourceId = getStorageId(sourceStorage);
                }

                String destinationId = getStorageId(destinationStorage);
                props.put("srcStorageId", sourceId);
                props.put("dstStorageId", destinationId);
                String messageId = authenticatedUser.getUsername() + "_" + sourceId + "_" + destinationId;
                String entityId = Utils.getId(messageId);
                properties.put("props", props);
                properties.put("tenantId", authenticatedUser.getTenantId());
                properties.put("entityId", entityId);
                properties.put("username", authenticatedUser.getUsername());
                properties.put("srcStorageId", sourceId);
                properties.put("dstStorageId", destinationId);
                properties.put("owner", authenticatedUser.getUsername());
                properties.put("entityId", entityId);
                props.put("entityId", entityId);

                if (hasAccess(authenticatedUser.getUsername(), authenticatedUser.getTenantId(), sourceId) &&
                        hasAccess(authenticatedUser.getUsername(), authenticatedUser.getTenantId(), destinationId)) {
                    String query = " Match (u:User), (srcSp:Storage), (dstSp:Storage) where " +
                            " u.username=$username AND u.tenantId=$tenantId AND " +
                            "srcSp.storageId=$srcStorageId AND " +
                            "srcSp.tenantId = $tenantId AND dstSp.storageId=$dstStorageId " +
                            "AND dstSp.tenantId =$tenantId " +
                            " Merge (u)-[:HAS_TRANSFER_MAPPING]->(tm:TransferMapping{entityId:$entityId, tenantId:$tenantId, " +
                            "srcStorageId:$srcStorageId," +
                            "dstStorageId:$dstStorageId,owner:$owner}) set tm += $props" +
                            " Merge (tm)<-[:TRANSFER_OUT]-(srcSp)" +
                            " Merge (tm)-[:TRANSFER_IN]->(dstSp) return (tm)";
                    this.neo4JConnector.runTransactionalQuery(properties, query);

                    String searchQuery = " Match (srcStr:Storage)-[:TRANSFER_OUT]->(tm:TransferMapping)" +
                            "-[:TRANSFER_IN]->(dstStr:Storage)  where " +
                            " tm.entityId=$entityId AND tm.tenantId=$tenantId return srcStr,  dstStr,  tm";
                    List<Record> records = this.neo4JConnector.searchNodes(properties, searchQuery);
                    if (!records.isEmpty()) {
                        transferMappings = TransferMappingDeserializer.deserializeList(records);
                    }
                }
            }

            if (!transferMappings.isEmpty()) {
                CreateTransferMappingResponse response = CreateTransferMappingResponse
                        .newBuilder()
                        .setTransferMapping(transferMappings.get(0))
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                String msg = "Errored while creating transfer mapping; Message:";
                logger.error("Errored while creating transfer mapping; Message:");
                responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
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
            properties.put("username", authenticatedUser.getUsername());
            properties.put("tenantId", authenticatedUser.getTenantId());
            properties.put("scope", TransferScope.USER.name());
            String query = " MATCH (u:User)-[:HAS_TRANSFER_MAPPING]->(t:TransferMapping{scope:$scope}) where u.username = $username AND u.tenantId = $tenantId" +
                    " Match (srcStr:Storage)-[:TRANSFER_OUT]->(t)-[:TRANSFER_IN]->(dstStr:Storage)" +
                    " return srcStr,  dstStr,  t";
            List<Record> records = this.neo4JConnector.searchNodes(properties, query);
            properties.put("scope", TransferScope.GLOBAL.name());
            String queryFetchGlobal = "Match (srcStr:Storage)-[:TRANSFER_OUT]->(t:TransferMapping{scope:$scope, tenantId:$tenantId})-[:TRANSFER_IN]->(dstStr:Storage)" +
                    " return srcStr,  dstStr,  t";
            List<Record> globalRecords = this.neo4JConnector.searchNodes(properties, queryFetchGlobal);
            if (!records.isEmpty()) {
                transferMappings = TransferMappingDeserializer.deserializeList(records);
            }
            if (!globalRecords.isEmpty()) {
                transferMappings.addAll(TransferMappingDeserializer.deserializeList(globalRecords));
            }
            FindTransferMappingsResponse findTransferMappingsResponse = FindTransferMappingsResponse
                    .newBuilder()
                    .addAllMappings(transferMappings)
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
            properties.put("username", authenticatedUser.getUsername());
            properties.put("tenantId", authenticatedUser.getTenantId());
            properties.put("entityId", transferMappingId);
            String query = " MATCH (u:User)-[:HAS_TRANSFER_MAPPING]->" +
                    "(t:TransferMapping{entityId:$entityId})" +
                    " where u.username = $username AND u.tenantId = $tenantId detach delete t";
            this.neo4JConnector.runTransactionalQuery(properties, query);
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

    private boolean hasAccess(String username, String tenantId, String storageId) throws Exception {
        Map<String, Object> userProps = new HashMap<>();
        userProps.put("username", username);
        userProps.put("tenantId", tenantId);
        userProps.put("entityId", storageId);

        List<Record> records = this.neo4JConnector.searchNodes(userProps,
                " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId" +
                        " OPTIONAL MATCH (u)<-[:SHARED_WITH]-(s1:Storage{entityId:$entityId})<-[:CHILD_OF]->(sp1:StoragePreference)" +
                        " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g:Group)<-[:MEMBER_OF]-(u)" +
                        " OPTIONAL MATCH (sp2:StoragePreference)-[:CHILD_OF]->(s2:Storage{entityId:$entityId})-[:SHARED_WITH]->(cg) " +
                        " OPTIONAL MATCH (sp3:StoragePreference )-[:CHILD_OF]->(s3:Storage{entityId:$entityId})-[:SHARED_WITH]->(g)" +
                        " OPTIONAL MATCH (s4:Storage{entityId:$entityId})<-[:CHILD_OF]->(sp4:StoragePreference)-[:SHARED_WITH]->(u)" +
                        " OPTIONAL MATCH (s5:Storage{entityId:$entityId})<-[:CHILD_OF]->(sp5:StoragePreference)-[:SHARED_WITH]->(cg)" +
                        " OPTIONAL MATCH (s6:Storage{entityId:$entityId})<-[:CHILD_OF]->(sp6:StoragePreference)-[:SHARED_WITH]->(g)" +
                        " return distinct s1, sp1, s2, sp2, s3, sp3, s4,sp4, s5,sp5, s6,sp6");
        if (!records.isEmpty()) {
            return true;
        }
        return false;
    }
}
