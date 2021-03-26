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
package org.apache.airavata.drms.api;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.groups.*;
import org.apache.airavata.datalake.drms.storage.*;

public class Client {
    public static void main(String ar[]) {

        DRMSServiceAuthToken token1 = DRMSServiceAuthToken.newBuilder().setAccessToken("Token-1").build();
        DRMSServiceAuthToken token2 = DRMSServiceAuthToken.newBuilder().setAccessToken("Token-2").build();

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();
        StorageServiceGrpc.StorageServiceBlockingStub storageClient = StorageServiceGrpc.newBlockingStub(channel);

        System.out.println("List for user 1");
        StorageSearchResponse storages = storageClient.searchStorage(
                StorageSearchRequest.newBuilder().setAuthToken(token1).build());
        System.out.println(storages);

        System.out.println("List for user 2");
        storages = storageClient.searchStorage(
                StorageSearchRequest.newBuilder().setAuthToken(token2).build());
        System.out.println(storages);

        System.out.println("Fetch");
        StorageFetchResponse fetchResponse = storageClient.fetchStorage(
                StorageFetchRequest.newBuilder().setAuthToken(token1).setStorageId("staging_pga_storage").buildPartial());

        System.out.println(fetchResponse);

        GroupServiceGrpc.GroupServiceBlockingStub groupClient = GroupServiceGrpc.newBlockingStub(channel);

        System.out.println("User");
        FetchCurrentUserResponse currentUser = groupClient.fetchCurrentUser(
                FetchCurrentUserRequest.newBuilder().setAuthToken(token1).build());
        System.out.println(currentUser);

        System.out.println("Groups");
        FetchCurrentUserGroupsResponse currentGroups = groupClient.fetchCurrentUserGroups(
                FetchCurrentUserGroupsRequest.newBuilder().setAuthToken(token2).build());
        System.out.println(currentGroups);

        System.out.println("Adding metadata");
        storageClient.addStorageMetadata(AddStorageMetadataRequest.newBuilder()
                .setAuthToken(token1)
                .setStorageId("prod_pga")
                .setKey("createdOn")
                .setValue("02/15/2021")
                .build());
    }
}
