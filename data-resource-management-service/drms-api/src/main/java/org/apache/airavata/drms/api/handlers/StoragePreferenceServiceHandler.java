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
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.groups.FetchCurrentUserRequest;
import org.apache.airavata.datalake.drms.groups.FetchCurrentUserResponse;
import org.apache.airavata.datalake.drms.groups.GroupServiceGrpc;
import org.apache.airavata.datalake.drms.groups.User;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.airavata.drms.core.deserializer.AnyStorageDeserializer;
import org.apache.airavata.drms.core.deserializer.AnyStoragePreferenceDeserializer;
import org.lognet.springboot.grpc.GRpcService;
import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GRpcService
public class StoragePreferenceServiceHandler extends StoragePreferenceServiceGrpc.StoragePreferenceServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StoragePreferenceServiceHandler.class);

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
        super.createStoragePreference(request, responseObserver);
    }

    @Override
    public void updateStoragePreference(StoragePreferenceUpdateRequest request, StreamObserver<StoragePreferenceUpdateResponse> responseObserver) {
        super.updateStoragePreference(request, responseObserver);
    }

    @Override
    public void deletePreferenceStorage(StoragePreferenceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        super.deletePreferenceStorage(request, responseObserver);
    }

    @Override
    public void searchStoragePreference(StoragePreferenceSearchRequest request, StreamObserver<StoragePreferenceSearchResponse> responseObserver) {
        AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

        List<Record> records = this.neo4JConnector.searchNodes(
                "MATCH (u:User)-[r1:MEMBER_OF]->(g:Group)<-[r2:SHARED_WITH]-(s:Storage)-[r3:HAS_PREFERENCE]->(sp:StoragePreference), " +
                        "(u)-[r4:MEMBER_OF]->(g2:Group)<-[r5:SHARED_WITH]-(sp)" +
                        " where u.userId ='" + callUser.getUsername() + "' return distinct sp, s");
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
