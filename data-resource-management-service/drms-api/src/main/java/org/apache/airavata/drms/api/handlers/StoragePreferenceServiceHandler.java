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
import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.airavata.drms.core.constants.StoragePreferenceConstants;
import org.apache.airavata.drms.core.deserializer.AnyStoragePreferenceDeserializer;
import org.apache.airavata.drms.core.serializer.AnyStoragePreferenceSerializer;
import org.lognet.springboot.grpc.GRpcService;
import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GRpcService
public class StoragePreferenceServiceHandler extends StoragePreferenceServiceGrpc.StoragePreferenceServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StoragePreferenceServiceHandler.class);

    @Autowired
    private Neo4JConnector neo4JConnector;


    @Override
    public void fetchStoragePreference(StoragePreferenceFetchRequest request, StreamObserver<StoragePreferenceFetchResponse> responseObserver) {
        AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

        List<Record> records = this.neo4JConnector.searchNodes(
                "MATCH (u:User)-[r1:MEMBER_OF]->(g:Group)<-[r2:SHARED_WITH]-(s:Storage)-[r3:HAS_PREFERENCE]->(sp:StoragePreference), " +
                        "(u)-[r4:MEMBER_OF]->(g2:Group)<-[r5:SHARED_WITH]-(sp) " +
                        "where sp.storagePreferenceId = '" + request.getStoragePreferenceId() + "' and u.userId = '"
                        + callUser.getUsername() + "' return distinct sp, s");

        if (!records.isEmpty()) {
            try {
                List<AnyStoragePreference> storagePrefList = AnyStoragePreferenceDeserializer.deserializeList(records);
                responseObserver.onNext(StoragePreferenceFetchResponse.newBuilder().setStoragePreference(
                        storagePrefList.get(0)).build());
                responseObserver.onCompleted();
            } catch (Exception e) {

                logger.error("Errored while fetching storage preference with id {}", request.getStoragePreferenceId(), e);
                responseObserver.onError(new Exception("Errored while fetching storage preference with id "
                        + request.getStoragePreferenceId() + ". Msg " + e.getMessage()));
            }
        } else {
            logger.error("Could not find a storage preference with id {}", request.getStoragePreferenceId());
            responseObserver.onError(new Exception("Could not find a storage preference with id "
                    + request.getStoragePreferenceId()));
        }
    }

    @Override
    public void createStoragePreference(StoragePreferenceCreateRequest request, StreamObserver<StoragePreferenceCreateResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            AnyStoragePreference storage = request.getStoragePreference();
            Map<String, Object> serializedMap = AnyStoragePreferenceSerializer.serializeToMap(storage);
            String storageId = (String) serializedMap.get("storagePreferenceId");
            this.neo4JConnector.mergeNode(serializedMap, StoragePreferenceConstants.STORAGE_PREFERENCE_TYPE_LABEL, callUser.getUsername(), storageId,
                    callUser.getTenantId());
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
            String storageId = (String) serializedMap.get("storagePreferenceId");
            this.neo4JConnector.mergeNode(serializedMap, StoragePreferenceConstants.STORAGE_PREFERENCE_TYPE_LABEL, callUser.getUsername(), storageId,
                    callUser.getTenantId());
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
        AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

        Map<String, Object> userProps = new HashMap<>();
        userProps.put("username", callUser.getUsername());
        userProps.put("tenantId", callUser.getTenantId());

        List<Record> records = this.neo4JConnector.searchNodes(userProps,
                " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId" +
                        " OPTIONAL MATCH (u)<-[:SHARED_WITH]-(s1:Storage)-[:HAS_PREFERENCE]->(sp1:StoragePreference)" +
                        " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g:Group)<-[:MEMBER_OF]-(u)" +
                        " OPTIONAL MATCH (sp2:StoragePreference)<-[:HAS_PREFERENCE]-(s2:Storage)-[:SHARED_WITH]->(cg) " +
                        " OPTIONAL MATCH (sp3:StoragePreference)<-[:HAS_PREFERENCE]-(s3:Storage)-[:SHARED_WITH]->(g)" +
                        " OPTIONAL MATCH (s4:Storage)-[:HAS_PREFERENCE]->(sp4:StoragePreference)-[:SHARED_WITH]->(u)" +
                        " OPTIONAL MATCH (s5:Storage)-[:HAS_PREFERENCE]->(sp5:StoragePreference)-[:SHARED_WITH]->(cg)" +
                        " OPTIONAL MATCH (s6:Storage)-[:HAS_PREFERENCE]->(sp6:StoragePreference)-[:SHARED_WITH]->(g)" +
                        " return distinct s1, sp1, s2, sp2, s3, sp3, s4,sp4, s5,sp5, s6,sp6");
        try {
            List<AnyStoragePreference> storagePrefList = AnyStoragePreferenceDeserializer.deserializeList(records);
            StoragePreferenceSearchResponse.Builder builder = StoragePreferenceSearchResponse.newBuilder();
            builder.addAllStoragesPreference(storagePrefList);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Errored while searching storage preferences; Message: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }
}
