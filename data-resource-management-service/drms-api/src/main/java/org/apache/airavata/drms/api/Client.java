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
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.clients.core.ClientUtils;
import org.apache.custos.user.management.client.UserManagementClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class Client {
    public static void main(String ar[]) {

//        DRMSServiceAuthToken token1 = DRMSServiceAuthToken.newBuilder().setAccessToken("Token-1").build();
//        DRMSServiceAuthToken token2 = DRMSServiceAuthToken.newBuilder().setAccessToken("Token-2").build();
//
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();
//        StorageServiceGrpc.StorageServiceBlockingStub storageClient = StorageServiceGrpc.newBlockingStub(channel);
//
//        System.out.println("List for user 1");
//        StorageSearchResponse storages = storageClient.searchStorage(
//                StorageSearchRequest.newBuilder().setAuthToken(token1).build());
//        System.out.println(storages);
//
//        System.out.println("List for user 2");
//        storages = storageClient.searchStorage(
//                StorageSearchRequest.newBuilder().setAuthToken(token2).build());
//        System.out.println(storages);
//
//        System.out.println("Fetch");
//        StorageFetchResponse fetchResponse = storageClient.fetchStorage(
//                StorageFetchRequest.newBuilder().setAuthToken(token1).setStorageId("staging_pga_storage").buildPartial());
//
//        System.out.println(fetchResponse);
//
//        GroupServiceGrpc.GroupServiceBlockingStub groupClient = GroupServiceGrpc.newBlockingStub(channel);
//
//        System.out.println("User");
//        FetchCurrentUserResponse currentUser = groupClient.fetchCurrentUser(
//                FetchCurrentUserRequest.newBuilder().setAuthToken(token1).build());
//        System.out.println(currentUser);
//
//        System.out.println("Groups");
//        FetchCurrentUserGroupsResponse currentGroups = groupClient.fetchCurrentUserGroups(
//                FetchCurrentUserGroupsRequest.newBuilder().setAuthToken(token2).build());
//        System.out.println(currentGroups);
//
//        System.out.println("Adding metadata");
//        storageClient.addStorageMetadata(AddStorageMetadataRequest.newBuilder()
//                .setAuthToken(token1)
//                .setStorageId("prod_pga")
//                .setKey("createdOn")
//                .setValue("02/15/2021")
//                .build());


        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceGrpc.newBlockingStub(channel);

        DRMSServiceAuthToken authToken = DRMSServiceAuthToken.newBuilder().
                setAccessToken("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4bTBuMk91R1hLckxMREZ3S0lBQ2J4T2JMYzUtY0MzRnFkT3M2akdLaGQ4In0.eyJqdGkiOiIwNWI5ZWY0ZS1lYzRkLTQyMWUtOWQ4Zi1lNmQ0NzEyMjczMWEiLCJleHAiOjE2MjA3NDkzODEsIm5iZiI6MCwiaWF0IjoxNjIwNzQ3NTgxLCJpc3MiOiJodHRwczovL2tleWNsb2FrLmN1c3Rvcy5zY2lnYXAub3JnOjMxMDAwL2F1dGgvcmVhbG1zLzEwMDAwNzAyIiwiYXVkIjpbInJlYWxtLW1hbmFnZW1lbnQiLCJhY2NvdW50Il0sInN1YiI6ImU4NzE2NjkzLWE5MzYtNDcwNy1hYjhhLWM2ZGMxYjNiOTcxMSIsInR5cCI6IkJlYXJlciIsImF6cCI6ImN1c3Rvcy1jbWNkY2xieXdseG1jMmt0enYwZC0xMDAwMDcwMiIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjQ4ZjRmYWQyLWE1OTYtNGMzYy1hMTU5LWVkNWY3MjJkOTQ0ZiIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiaHR0cHM6Ly90ZXN0ZHJpdmUudXNlY3VzdG9zLm9yZyJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJhZG1pbiIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsicmVhbG0tbWFuYWdlbWVudCI6eyJyb2xlcyI6WyJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsInJlYWxtLWFkbWluIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInF1ZXJ5LXJlYWxtcyIsInZpZXctYXV0aG9yaXphdGlvbiIsInF1ZXJ5LWNsaWVudHMiLCJxdWVyeS11c2VycyIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIiwicXVlcnktZ3JvdXBzIl19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJDdXN0b3MgQWRtaW5EIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdHVzZXIiLCJnaXZlbl9uYW1lIjoiQ3VzdG9zIiwiZmFtaWx5X25hbWUiOiJBZG1pbkQiLCJlbWFpbCI6ImN1c3Rvcy1hZG1pbkBpdS5lZHUifQ.h4N2Xcg2n7Res6Y-t5OgGK_W1yRCWCUNoLnrCWM3mQRHcbT6Y0t9MwGMoFaA85rYtlLCh-LL9GN8nQ_TNhWgVcmGlY9UI7xaoGHwVyoHTuIqcy9Ojj-8FcAni0s7Ix_1PZ7OYWZLO8yVZDQCmR28lUWx644t8ENkuwix20yiXmWMyNP8xwBnfcQQWDcLTEk3N07dp5fqYKxFzTC13gAXl9HhRcnXrscHmAha-psLdGwz_yVsAhAjGRRBKbhqbLM4lL1YsWIWkHBIuUDbebrREYB6-xQgZEhLMMC2k2u6bDMPmOzhPYiY_V3S8f13jsyC-1NYFUekLAcoCOY-wx_soQ")
                .build();
        ResourceSearchRequest request = ResourceSearchRequest.newBuilder().setAuthToken(authToken).build();

        ResourceSearchResponse response =  resourceClient.searchResource(request);
        for (GenericResource resource: response.getResourcesList()) {
            System.out.println(resource.getType());
        }

//        try {
//            InputStream inputStream = ClientUtils.getServerCertificate("custos.scigap.org",
//                    "custos-2zuomcugra3ebgsqtzmf-10000514", "mupUhF4JL0S3IFHBjfhiTfLJS1NgSWfvkCj3l6c7");
//            byte[] buffer = new byte[inputStream.available()];
//            inputStream.read(buffer);
//
//            File targetFile = new File("/Users/isururanawaka/Documents/Airavata_Repository/airavata-data-lake/data-resource-management-service/drms-api/src/main/resources/tartget.tmp");
//            OutputStream outStream = new FileOutputStream(targetFile);
//            outStream.write(buffer);
//
//            CustosClientProvider custosClientProvider = new CustosClientProvider.Builder().setServerHost("custos.scigap.org")
//                    .setServerPort(31499)
//                    .setClientId("custos-2zuomcugra3ebgsqtzmf-10000514")
//                    .setClientSec("mupUhF4JL0S3IFHBjfhiTfLJS1NgSWfvkCj3l6c7").build();
//            UserManagementClient userManagementClient = custosClientProvider.getUserManagementClient();
//            userManagementClient.getUser("testuser", "custos-cmcdclbywlxmc2ktzv0d-10000702");
//
//        } catch (Exception ex) {
//
//        }
    }
}
