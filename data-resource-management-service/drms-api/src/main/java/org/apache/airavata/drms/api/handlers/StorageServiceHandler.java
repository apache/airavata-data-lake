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
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.groups.FetchCurrentUserRequest;
import org.apache.airavata.datalake.drms.groups.FetchCurrentUserResponse;
import org.apache.airavata.datalake.drms.groups.GroupServiceGrpc;
import org.apache.airavata.datalake.drms.groups.User;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.airavata.drms.core.deserializer.AnyStorageDeserializer;
import org.apache.airavata.drms.core.constants.StorageConstants;
import org.apache.airavata.drms.core.serializer.AnyStorageSerializer;
import org.lognet.springboot.grpc.GRpcService;
import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@GRpcService
public class StorageServiceHandler extends StorageServiceGrpc.StorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceHandler.class);

    @Autowired
    private Neo4JConnector neo4JConnector;

    @org.springframework.beans.factory.annotation.Value("${group.service.host}")
    private String groupServiceHost;

    @org.springframework.beans.factory.annotation.Value("${group.service.port}")
    private int groupServicePort;


    private User getUser(DRMSServiceAuthToken authToken) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(groupServiceHost, groupServicePort).usePlaintext().build();
        GroupServiceGrpc.GroupServiceBlockingStub groupClient = GroupServiceGrpc.newBlockingStub(channel);
        FetchCurrentUserResponse userResponse = groupClient.fetchCurrentUser(
                FetchCurrentUserRequest.newBuilder().setAuthToken(authToken).build());
        return userResponse.getUser();
    }

    @Override
    public void fetchStorage(StorageFetchRequest request, StreamObserver<StorageFetchResponse> responseObserver) {

        User callUser = getUser(request.getAuthToken());

        List<Record> records = this.neo4JConnector.searchNodes(
                "MATCH (u:User)-[r1:MEMBER_OF]->(g:Group)<-[r2:SHARED_WITH]-(s:Storage) where " +
                        "s.storageId = '" + request.getStorageId() + "' and u.userId = '" + callUser.getUserId() +
                        "' return distinct s");

        if (!records.isEmpty()) {
            try {
                List<AnyStorage> storageList = AnyStorageDeserializer.deserializeList(records);
                responseObserver.onNext(StorageFetchResponse.newBuilder().setStorage(storageList.get(0)).build());
                responseObserver.onCompleted();
            } catch (Exception e) {

                logger.error("Errored while fetching storage with id {}", request.getStorageId(), e);
                responseObserver.onError(new Exception("Errored while fetching storage with id " + request.getStorageId()
                        + ". Msg " + e.getMessage()));
            }
        } else {
            logger.error("Could not find a storage with id {}", request.getStorageId());
            responseObserver.onError(new Exception("Could not find a storage with id " + request.getStorageId()));
        }
    }

    @Override
    public void createStorage(StorageCreateRequest request, StreamObserver<StorageCreateResponse> responseObserver) {
        User user = getUser(request.getAuthToken());
        AnyStorage storage = request.getStorage();
        Map<String, Object> serializedMap = AnyStorageSerializer.serializeToMap(storage);
        this.neo4JConnector.createNode(serializedMap, StorageConstants.STORAGE_LABEL, user.getUserId());
    }

    @Override
    public void updateStorage(StorageUpdateRequest request, StreamObserver<StorageUpdateResponse> responseObserver) {
        super.updateStorage(request, responseObserver);
    }

    @Override
    public void deleteStorage(StorageDeleteRequest request, StreamObserver<Empty> responseObserver) {
        super.deleteStorage(request, responseObserver);
    }

    @Override
    public void addStorageMetadata(AddStorageMetadataRequest request, StreamObserver<Empty> responseObserver) {
        User callUser = getUser(request.getAuthToken());
        this.neo4JConnector.createMetadataNode(StorageConstants.STORAGE_LABEL, "storageId",
                request.getStorageId(), callUser.getUserId(),
                request.getKey(), request.getValue());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void searchStorage(StorageSearchRequest request, StreamObserver<StorageSearchResponse> responseObserver) {
        User callUser = getUser(request.getAuthToken());

        List<Record> records = this.neo4JConnector.searchNodes(
                "MATCH (u:User)-[r1:MEMBER_OF]->(g:Group)<-[r2:SHARED_WITH]-(s:Storage) where u.userId ='" +
                        callUser.getUserId() + "' return distinct s");
        try {
            List<AnyStorage> storageList = AnyStorageDeserializer.deserializeList(records);
            StorageSearchResponse.Builder builder = StorageSearchResponse.newBuilder();
            builder.addAllStorages(storageList);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Errored while searching storages; Message: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }
}
