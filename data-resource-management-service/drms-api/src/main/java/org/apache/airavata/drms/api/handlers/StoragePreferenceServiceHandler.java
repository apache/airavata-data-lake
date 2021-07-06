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
import org.apache.airavata.drms.api.utils.CustosUtils;
import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.airavata.drms.core.constants.StorageConstants;
import org.apache.airavata.drms.core.constants.StoragePreferenceConstants;
import org.apache.airavata.drms.core.deserializer.AnyStoragePreferenceDeserializer;
import org.apache.airavata.drms.core.deserializer.TransferMappingDeserializer;
import org.apache.airavata.drms.core.serializer.AnyStoragePreferenceSerializer;
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
public class StoragePreferenceServiceHandler extends StoragePreferenceServiceGrpc.StoragePreferenceServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StoragePreferenceServiceHandler.class);

    @Autowired
    private Neo4JConnector neo4JConnector;

    @Autowired
    private CustosClientProvider custosClientProvider;

    public StoragePreferenceServiceHandler() {

    }


    @Override
    public void fetchStoragePreference(StoragePreferenceFetchRequest request, StreamObserver<StoragePreferenceFetchResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            Map<String, Object> userProps = new HashMap<>();
            userProps.put("username", callUser.getUsername());
            userProps.put("tenantId", callUser.getTenantId());
            userProps.put("storagePreferenceId", request.getStoragePreferenceId());

            List<Record> records = this.neo4JConnector.searchNodes(userProps,
                    " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId" +
                            " OPTIONAL MATCH (u)<-[:SHARED_WITH]-(s1:Storage)<-[:CHILD_OF]->(sp1:StoragePreference) where sp1.storagePreferenceId = $storagePreferenceId" +
                            " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g:Group)<-[:MEMBER_OF]-(u)" +
                            " OPTIONAL MATCH (sp2:StoragePreference)-[:CHILD_OF]->(s2:Storage)-[:SHARED_WITH]->(cg) where sp2.storagePreferenceId = $storagePreferenceId" +
                            " OPTIONAL MATCH (sp3:StoragePreference)-[:CHILD_OF]->(s3:Storage)-[:SHARED_WITH]->(g) where sp3.storagePreferenceId = $storagePreferenceId" +
                            " OPTIONAL MATCH (s4:Storage)<-[:CHILD_OF]->(sp4:StoragePreference)-[:SHARED_WITH]->(u) where sp4.storagePreferenceId = $storagePreferenceId" +
                            " OPTIONAL MATCH (s5:Storage)<-[:CHILD_OF]->(sp5:StoragePreference)-[:SHARED_WITH]->(cg) where sp5.storagePreferenceId = $storagePreferenceId" +
                            " OPTIONAL MATCH (s6:Storage)<-[:CHILD_OF]->(sp6:StoragePreference)-[:SHARED_WITH]->(g) where sp6.storagePreferenceId = $storagePreferenceId" +
                            " return distinct s1, sp1, s2, sp2, s3, sp3, s4,sp4, s5,sp5, s6,sp6");

            if (!records.isEmpty()) {
                try {
                    List keyList = new ArrayList();
                    keyList.add("s1:sp1");
                    keyList.add("s2:sp2");
                    keyList.add("s3:sp3");
                    keyList.add("s4:sp4");
                    keyList.add("s5:sp5");
                    keyList.add("s6:sp6");
                    List<AnyStoragePreference> storagePrefList = AnyStoragePreferenceDeserializer.deserializeList(records, keyList);
                    StoragePreferenceFetchResponse.Builder builder = StoragePreferenceFetchResponse.newBuilder();
                    if (!storagePrefList.isEmpty()) {
                        builder.setStoragePreference(storagePrefList.get(0));
                    }
                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();
                } catch (Exception e) {
                    String msg = "Errored while searching storage preferences; Message:" + e.getMessage();
                    logger.error("Errored while searching storage preferences; Message: {}", e.getMessage(), e);
                    responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
                }
            } else {
                String msg = "Could not find a storage preference with id" + request.getStoragePreferenceId();
                logger.error("Could not find a storage preference with id {}", request.getStoragePreferenceId());
                responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
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
            Map<String, Object> parentPropertiesMap = null;
            if (storage.getStorageCase().equals(AnyStoragePreference.StorageCase.S3_STORAGE_PREFERENCE)) {
                storageId = storage.getS3StoragePreference().getStorage().getStorageId();
                parentPropertiesMap = AnyStorageSerializer.serializeToMap(AnyStorage
                        .newBuilder().setS3Storage(storage.getS3StoragePreference().getStorage()).build());

            } else if (storage.getStorageCase()
                    .equals(AnyStoragePreference.StorageCase.SSH_STORAGE_PREFERENCE)) {

                storageId = storage.getSshStoragePreference().getStorage().getStorageId();
                parentPropertiesMap = AnyStorageSerializer.serializeToMap(AnyStorage
                        .newBuilder().setSshStorage(storage.getSshStoragePreference().getStorage()).build());
            }
            if (storageId != null) {
                CustosUtils.
                        mergeStoragePreferenceEntity(custosClientProvider, callUser.getTenantId(),
                                storagePreferenceId, storageId, callUser.getUsername());

                this.neo4JConnector.mergeNodesWithParentChildRelationShip(serializedMap, parentPropertiesMap,
                        StoragePreferenceConstants.STORAGE_PREFERENCE_LABEL, StorageConstants.STORAGE_LABEL,
                        callUser.getUsername(), storagePreferenceId, storageId, callUser.getTenantId());
            }
            StoragePreferenceCreateResponse response = StoragePreferenceCreateResponse.newBuilder().setStoragePreference(storage).build();
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
            Map<String, Object> parentPropertiesMap = null;
            if (storage.getStorageCase().name()
                    .equals(AnyStoragePreference.StorageCase.S3_STORAGE_PREFERENCE)) {
                storageId = storage.getS3StoragePreference().getStorage().getStorageId();
                parentPropertiesMap = AnyStorageSerializer.serializeToMap(AnyStorage
                        .newBuilder().setS3Storage(storage.getS3StoragePreference().getStorage()).build());

            } else if (storage.getStorageCase()
                    .equals(AnyStoragePreference.StorageCase.SSH_STORAGE_PREFERENCE)) {

                storageId = storage.getSshStoragePreference().getStorage().getStorageId();
                parentPropertiesMap = AnyStorageSerializer.serializeToMap(AnyStorage
                        .newBuilder().setSshStorage(storage.getSshStoragePreference().getStorage()).build());
            }
            if (storageId != null) {
                CustosUtils.
                        mergeStoragePreferenceEntity(custosClientProvider, callUser.getTenantId(),
                                storagePreferenceId, storageId, callUser.getUsername());

                this.neo4JConnector.mergeNodesWithParentChildRelationShip(serializedMap, parentPropertiesMap,
                        StoragePreferenceConstants.STORAGE_PREFERENCE_LABEL, StorageConstants.STORAGE_LABEL,
                        callUser.getUsername(), storagePreferenceId, storageId, callUser.getTenantId());
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
            this.neo4JConnector.deleteNode(StoragePreferenceConstants.STORAGE_PREFERENCE_LABEL, id, callUser.getTenantId());
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
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

            Map<String, Object> userProps = new HashMap<>();
            userProps.put("username", callUser.getUsername());
            userProps.put("tenantId", callUser.getTenantId());

            List<Record> records = this.neo4JConnector.searchNodes(userProps,
                    " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId" +
                            " OPTIONAL MATCH (u)<-[:SHARED_WITH]-(s1:Storage)<-[:CHILD_OF]->(sp1:StoragePreference)" +
                            " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g:Group)<-[:MEMBER_OF]-(u)" +
                            " OPTIONAL MATCH (sp2:StoragePreference)-[:CHILD_OF]->(s2:Storage)-[:SHARED_WITH]->(cg) " +
                            " OPTIONAL MATCH (sp3:StoragePreference)-[:CHILD_OF]->(s3:Storage)-[:SHARED_WITH]->(g)" +
                            " OPTIONAL MATCH (s4:Storage)<-[:CHILD_OF]->(sp4:StoragePreference)-[:SHARED_WITH]->(u)" +
                            " OPTIONAL MATCH (s5:Storage)<-[:CHILD_OF]->(sp5:StoragePreference)-[:SHARED_WITH]->(cg)" +
                            " OPTIONAL MATCH (s6:Storage)<-[:CHILD_OF]->(sp6:StoragePreference)-[:SHARED_WITH]->(g)" +
                            " return distinct s1, sp1, s2, sp2, s3, sp3, s4,sp4, s5,sp5, s6,sp6");
            if (!records.isEmpty()) {
                try {
                    List keyList = new ArrayList();
                    keyList.add("s1:sp1");
                    keyList.add("s2:sp2");
                    keyList.add("s3:sp3");
                    keyList.add("s4:sp4");
                    keyList.add("s5:sp5");
                    keyList.add("s6:sp6");
                    List<AnyStoragePreference> storagePrefList = AnyStoragePreferenceDeserializer.deserializeList(records, keyList);
                    StoragePreferenceSearchResponse.Builder builder = StoragePreferenceSearchResponse.newBuilder();
                    builder.addAllStoragesPreference(storagePrefList);
                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();
                } catch (Exception e) {
                    String msg = "Errored while searching storage preferences; Message:" + e.getMessage();
                    logger.error("Errored while searching storage preferences; Message: {}", e.getMessage(), e);
                    responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
                }
            } else {
                responseObserver.onNext(StoragePreferenceSearchResponse.newBuilder().build());
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            String msg = "Errored while searching storage preferences; Message:" + e.getMessage();
            logger.error("Errored while searching storage preferences; Message: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void createTransferMapping(CreateTransferMappingRequest request, StreamObserver<CreateTransferMappingResponse> responseObserver) {
        try {
            AuthenticatedUser authenticatedUser = request.getAuthToken().getAuthenticatedUser();
            AnyStoragePreference sourceStoragePreference = request.getTransferMapping().getSourceStoragePreference();
            AnyStoragePreference destinationStoragePreference = request.getTransferMapping().getDestinationStoragePreference();
            String sourceId = getStorageId(sourceStoragePreference);
            String destinationId = getStorageId(destinationStoragePreference);

            TransferScope scope = request.getTransferMapping().getTransferScope();
            Map<String, Object> properties = new HashMap<>();
            Map<String, Object> props = new HashMap<>();
            props.put("tenantId", authenticatedUser.getTenantId());
            props.put("owner", authenticatedUser.getUsername());
            props.put("srcStoragePreferenceId", sourceId);
            props.put("dstStoragePreferenceId", destinationId);
            String entityId = sourceId + "_" + destinationId;
            if (scope.equals(TransferScope.GLOBAL)) {
                props.put("scope", TransferScope.GLOBAL.name());
            } else {
                props.put("scope", TransferScope.USER.name());
            }
            properties.put("props", props);
            properties.put("tenantId", authenticatedUser.getTenantId());
            properties.put("entityId", entityId);
            properties.put("username", authenticatedUser.getUsername());
            properties.put("srcStoragePreferenceId", sourceId);
            properties.put("dstStoragePreferenceId", destinationId);
            properties.put("owner", authenticatedUser.getUsername());


            if (hasAccess(authenticatedUser.getUsername(), authenticatedUser.getTenantId(), sourceId) &&
                    hasAccess(authenticatedUser.getUsername(), authenticatedUser.getTenantId(), destinationId)) {
                String query = " Match (u:User), (srcSp:StoragePreference), (dstSp:StoragePreference) where " +
                        " u.username=$username AND u.tenantId=$tenantId AND " +
                        "srcSp.storagePreferenceId=$srcStoragePreferenceId AND " +
                        "srcSp.tenantId = $tenantId AND dstSp.storagePreferenceId=$dstStoragePreferenceId " +
                        "AND dstSp.tenantId =$tenantId " +
                        " Merge (u)-[:HAS_TRANSFER_MAPPING]->(tm:TransferMapping{entityId:$entityId, tenantId:$tenantId, " +
                        "srcStoragePreferenceId:$srcStoragePreferenceId," +
                        "dstStoragePreferenceId:$dstStoragePreferenceId,owner:$owner}) set tm += $props" +
                        " Merge (tm)<-[:TRANSFER_OUT]-(srcSp)" +
                        " Merge (tm)-[:TRANSFER_IN]->(dstSp) return (tm)";
                this.neo4JConnector.runTransactionalQuery(properties, query);

                String searchQuery = " Match (srcStr:Storage)<-[:CHILD_OF]-" +
                        "(srcSp:StoragePreference)-[:TRANSFER_OUT]->(tm:TransferMapping)" +
                        "-[:TRANSFER_IN]->(dstSp:StoragePreference)-[:CHILD_OF]->(dstStr:Storage)  where " +
                        " tm.entityId=$entityId AND tm.tenantId=$tenantId return srcStr, srcSp, dstStr, dstSp, tm";
                List<Record> records = this.neo4JConnector.searchNodes(properties, searchQuery);
                if (!records.isEmpty()) {
                    List<TransferMapping> transferMappings = TransferMappingDeserializer.deserializeList(records);
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
                } else {
                    String msg = "Errored while creating transfer mapping; Message:";
                    logger.error("Errored while creating transfer mapping; Message:");
                    responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(msg).asRuntimeException());
                }
            } else {
                String msg = "User does not have permission to create mapping ";
                logger.error("User does not have permission to create mapping ");
                responseObserver.onError(Status.PERMISSION_DENIED.withDescription(msg).asRuntimeException());

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
                    " Match (srcStr:Storage)<-[:CHILD_OF]-(srcSp:StoragePreference)-[:TRANSFER_OUT]->(t)-[:TRANSFER_IN]->(dstSp:StoragePreference)-[:CHILD_OF]->(dstStr:Storage)" +
                    " return srcStr, srcSp, dstStr, dstSp, t";
            List<Record> records = this.neo4JConnector.searchNodes(properties, query);
            properties.put("scope", TransferScope.GLOBAL.name());
            String queryFetchGlobal = "Match (srcStr:Storage)<-[:CHILD_OF]-" +
                    "(srcSp:StoragePreference)-[:TRANSFER_OUT]->(t:TransferMapping{scope:$scope, tenantId:$tenantId})-[:TRANSFER_IN]->(dstSp:StoragePreference)-[:CHILD_OF]->(dstStr:Storage)" +
                    " return srcStr, srcSp, dstStr, dstSp, t";
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

    private boolean hasAccess(String username, String tenantId, String storagePrefId) throws Exception {
        Map<String, Object> userProps = new HashMap<>();
        userProps.put("username", username);
        userProps.put("tenantId", tenantId);
        userProps.put("entityId", storagePrefId);

        List<Record> records = this.neo4JConnector.searchNodes(userProps,
                " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId" +
                        " OPTIONAL MATCH (u)<-[:SHARED_WITH]-(s1:Storage)<-[:CHILD_OF]->(sp1:StoragePreference{entityId:$entityId})" +
                        " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g:Group)<-[:MEMBER_OF]-(u)" +
                        " OPTIONAL MATCH (sp2:StoragePreference{entityId:$entityId})-[:CHILD_OF]->(s2:Storage)-[:SHARED_WITH]->(cg) " +
                        " OPTIONAL MATCH (sp3:StoragePreference {entityId:$entityId})-[:CHILD_OF]->(s3:Storage)-[:SHARED_WITH]->(g)" +
                        " OPTIONAL MATCH (s4:Storage)<-[:CHILD_OF]->(sp4:StoragePreference{entityId:$entityId})-[:SHARED_WITH]->(u)" +
                        " OPTIONAL MATCH (s5:Storage)<-[:CHILD_OF]->(sp5:StoragePreference{entityId:$entityId})-[:SHARED_WITH]->(cg)" +
                        " OPTIONAL MATCH (s6:Storage)<-[:CHILD_OF]->(sp6:StoragePreference{entityId:$entityId})-[:SHARED_WITH]->(g)" +
                        " return distinct s1, sp1, s2, sp2, s3, sp3, s4,sp4, s5,sp5, s6,sp6");
        if (!records.isEmpty()) {
            return true;
        }
        return false;
    }

    private String getStorageId(AnyStoragePreference storage) {
        if (storage.getStorageCase()
                .equals(AnyStoragePreference.StorageCase.S3_STORAGE_PREFERENCE)) {
            return storage.getS3StoragePreference().getStoragePreferenceId();
        } else {
            return storage.getSshStoragePreference().getStoragePreferenceId();
        }
    }
}
