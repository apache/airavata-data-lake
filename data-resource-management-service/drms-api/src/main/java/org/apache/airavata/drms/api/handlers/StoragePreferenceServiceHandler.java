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

            String query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId" +
                    " OPTIONAL MATCH (u)<-[:SHARED_WITH]-(s1:Storage)<-[:CHILD_OF]->(sp1:StoragePreference)" +
                    " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g:Group)<-[:MEMBER_OF]-(u)" +
                    " OPTIONAL MATCH (sp2:StoragePreference)-[:CHILD_OF]->(s2:Storage)-[:SHARED_WITH]->(cg) " +
                    " OPTIONAL MATCH (sp3:StoragePreference)-[:CHILD_OF]->(s3:Storage)-[:SHARED_WITH]->(g)" +
                    " OPTIONAL MATCH (s4:Storage)<-[:CHILD_OF]->(sp4:StoragePreference)-[:SHARED_WITH]->(u)" +
                    " OPTIONAL MATCH (s5:Storage)<-[:CHILD_OF]->(sp5:StoragePreference)-[:SHARED_WITH]->(cg)" +
                    " OPTIONAL MATCH (s6:Storage)<-[:CHILD_OF]->(sp6:StoragePreference)-[:SHARED_WITH]->(g)" +
                    " return distinct s1, sp1, s2, sp2, s3, sp3, s4,sp4, s5,sp5, s6,sp6";


            String storageId = null;
            List<StoragePreferenceSearchQuery> storagePreferenceSearchQueries = request.getQueriesList();

            for (StoragePreferenceSearchQuery searchQuery : storagePreferenceSearchQueries) {
                if (searchQuery.getField().equals("storageId")) {
                    storageId = searchQuery.getValue();
                }
            }

            if (storageId != null) {
                query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId" +
                        " OPTIONAL MATCH (u)<-[:SHARED_WITH]-(s1:Storage{storageId:'" + storageId + "'})<-[:CHILD_OF]->(sp1:StoragePreference)" +
                        " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g:Group)<-[:MEMBER_OF]-(u)" +
                        " OPTIONAL MATCH (sp2:StoragePreference)-[:CHILD_OF]->(s2:Storage{storageId:'" + storageId + "'})-[:SHARED_WITH]->(cg) " +
                        " OPTIONAL MATCH (sp3:StoragePreference)-[:CHILD_OF]->(s3:Storage{storageId:'" + storageId + "'})-[:SHARED_WITH]->(g)" +
                        " OPTIONAL MATCH (s4:Storage{storageId:'" + storageId + "'})<-[:CHILD_OF]->(sp4:StoragePreference)-[:SHARED_WITH]->(u)" +
                        " OPTIONAL MATCH (s5:Storage{storageId:'" + storageId + "'})<-[:CHILD_OF]->(sp5:StoragePreference)-[:SHARED_WITH]->(cg)" +
                        " OPTIONAL MATCH (s6:Storage{storageId:'" + storageId + "'})<-[:CHILD_OF]->(sp6:StoragePreference)-[:SHARED_WITH]->(g)" +
                        " return distinct s1, sp1, s2, sp2, s3, sp3, s4,sp4, s5,sp5, s6,sp6";
            }

            List<Record> records = this.neo4JConnector.searchNodes(userProps, query);

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





}
