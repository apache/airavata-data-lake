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

import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.drms.groups.*;
import org.lognet.springboot.grpc.GRpcService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GRpcService
public class GroupServiceHandler extends GroupServiceGrpc.GroupServiceImplBase {

    @Override
    public void fetchCurrentUser(FetchCurrentUserRequest request, StreamObserver<FetchCurrentUserResponse> responseObserver) {
        try {
            User user = getUserByAccessToken(request.getAuthToken().getAccessToken());
            responseObserver.onNext(FetchCurrentUserResponse.newBuilder().setUser(user).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void fetchUser(FetchUserRequest request, StreamObserver<FetchUserResponse> responseObserver) {
        super.fetchUser(request, responseObserver);
    }

    @Override
    public void fetchUserGroups(FetchUserGroupsRequest request, StreamObserver<FetchUserGroupsResponse> responseObserver) {
        super.fetchUserGroups(request, responseObserver);
    }

    @Override
    public void fetchCurrentUserGroups(FetchCurrentUserGroupsRequest request,
                                       StreamObserver<FetchCurrentUserGroupsResponse> responseObserver) {
        try {
            User user = getUserByAccessToken(request.getAuthToken().getAccessToken());
            List<Group> userGroups = getUserGroups(user.getUserId());
            responseObserver.onNext(FetchCurrentUserGroupsResponse.newBuilder().addAllGroups(userGroups).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private User getUserByAccessToken(String accessToken) throws Exception {
        switch (accessToken) {
            case "Token-1":
                return User.newBuilder().setUserId("user-1").setUsername("bob").setFirstName("Bob").setLastName("Leech")
                        .setEmailAddress("bob@gmail.com").build();
            case "Token-2":
                return User.newBuilder().setUserId("user-2").setUsername("alice").setFirstName("Alice").setLastName("Ann")
                        .setEmailAddress("alice@gmail.com").build();
        }
        throw new Exception("No user for given access token");
    }

    private List<Group> getUserGroups(String userId) {
        Map<String, Group> groups = new HashMap<>();
        List<Group> groupList = new ArrayList<>();
        groups.put("group-1", Group.newBuilder().setGroupId("group-1").setName("Teachers").setDescription("Teachers").build());
        groups.put("group-2", Group.newBuilder().setGroupId("group-2").setName("Students").setDescription("Students").build());

        switch (userId) {
            case "user-1":
                groupList.add(groups.get("group-1"));
                groupList.add(groups.get("group-2"));
                break;
            case "user-2":
                groupList.add(groups.get("group-2"));
                break;
        }
        return groupList;
    }
}
