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
import org.apache.airavata.drms.core.constants.StorageConstants;
import org.apache.airavata.drms.core.deserializer.AnyStorageDeserializer;
import org.apache.airavata.drms.core.serializer.AnyStorageSerializer;
import org.lognet.springboot.grpc.GRpcService;
import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GRpcService
public class StorageServiceHandler extends StorageServiceGrpc.StorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceHandler.class);

    @Autowired
    private Neo4JConnector neo4JConnector;


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
                        " OPTIONAL MATCH (ch:Group)-[CHILD_OF*]->(g:Group)<-[r3:MEMBER_OF]-(u)" +
                        " OPTIONAL MATCH (cs:Storage)-[SHARED_WITH]->(ch) " +
                        " OPTIONAL MATCH (ds:Storage)-[SHARED_WITH]->(g) return distinct s, cs, ds");
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
}
