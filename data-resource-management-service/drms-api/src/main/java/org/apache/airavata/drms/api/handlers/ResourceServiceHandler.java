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
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.airavata.drms.core.constants.ResourceConstants;
import org.apache.airavata.drms.core.constants.StorageConstants;
import org.apache.airavata.drms.core.deserializer.AnyStoragePreferenceDeserializer;
import org.apache.airavata.drms.core.deserializer.GenericResourceDeserializer;
import org.lognet.springboot.grpc.GRpcService;
import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GRpcService
public class ResourceServiceHandler extends ResourceServiceGrpc.ResourceServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceHandler.class);

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
    public void fetchResource(ResourceFetchRequest request, StreamObserver<ResourceFetchResponse> responseObserver) {
        User callUser = getUser(request.getAuthToken());

        // TODO review (u)-[r4:MEMBER_OF]->(g2:Group)<-[r5:SHARED_WITH]-(sp),
        List<Record> records = this.neo4JConnector.searchNodes(
                "MATCH (u:User)-[r1:MEMBER_OF]->(g:Group)<-[r2:SHARED_WITH]-(s:Storage)-[r3:HAS_PREFERENCE]->(sp:StoragePreference)-[r6:HAS_RESOURCE]->(res:Resource), " +
                        "(u)-[r7:MEMBER_OF]->(g3:Group)<-[r8:SHARED_WITH]-(res) " +
                        "where res.resourceId = '" + request.getResourceId() + "' and u.userId = '"
                        + callUser.getUserId() + "' return distinct res, sp, s");

        if (!records.isEmpty()) {
            try {
                List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
                responseObserver.onNext(ResourceFetchResponse.newBuilder().setResource(genericResourceList.get(0)).build());
                responseObserver.onCompleted();
            } catch (Exception e) {

                logger.error("Errored while fetching resource with id {}", request.getResourceId(), e);
                responseObserver.onError(new Exception("Errored while fetching resource with id "
                        + request.getResourceId() + ". Msg " + e.getMessage()));
            }
        } else {
            logger.error("Could not find a generic resource with id {}", request.getResourceId());
            responseObserver.onError(new Exception("Could not find a generic resource with id "
                    + request.getResourceId()));
        }
    }

    @Override
    public void createResource(ResourceCreateRequest request, StreamObserver<ResourceCreateResponse> responseObserver) {
        super.createResource(request, responseObserver);
    }

    @Override
    public void updateResource(ResourceUpdateRequest request, StreamObserver<ResourceUpdateResponse> responseObserver) {
        super.updateResource(request, responseObserver);
    }

    @Override
    public void deletePreferenceStorage(ResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        super.deletePreferenceStorage(request, responseObserver);
    }

    @Override
    public void searchResource(ResourceSearchRequest request, StreamObserver<ResourceSearchResponse> responseObserver) {
        User callUser = getUser(request.getAuthToken());

        // TODO review (u)-[r4:MEMBER_OF]->(g2:Group)<-[r5:SHARED_WITH]-(sp),
        List<Record> records = this.neo4JConnector.searchNodes(
                "MATCH (u:User)-[r1:MEMBER_OF]->(g:Group)<-[r2:SHARED_WITH]-(s:Storage)-[r3:HAS_PREFERENCE]->(sp:StoragePreference)-[r6:HAS_RESOURCE]->(res:Resource), " +
                        "(u)-[r7:MEMBER_OF]->(g3:Group)<-[r8:SHARED_WITH]-(res) " +
                        "where u.userId = '" + callUser.getUserId() + "' return distinct res, sp, s");
        try {
            List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
            ResourceSearchResponse.Builder builder = ResourceSearchResponse.newBuilder();
            builder.addAllResources(genericResourceList);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Errored while searching generic resources; Message: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void addResourceMetadata(AddResourceMetadataRequest request, StreamObserver<Empty> responseObserver) {
        User callUser = getUser(request.getAuthToken());
        this.neo4JConnector.createMetadataNode(ResourceConstants.RESOURCE_LABEL, "resourceId",
                request.getResourceId(), callUser.getUserId(),
                request.getMetadata().getKey(), request.getMetadata().getValue());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void fetchResourceMetadata(FetchResourceMetadataRequest request, StreamObserver<FetchResourceMetadataResponse> responseObserver) {
        super.fetchResourceMetadata(request, responseObserver);
    }
}
