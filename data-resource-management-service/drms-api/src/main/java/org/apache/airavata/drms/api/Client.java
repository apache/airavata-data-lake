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
import org.apache.airavata.datalake.drms.storage.*;

public class Client {
    public static void main(String ar[]) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 7070).usePlaintext().build();
//        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceGrpc.newBlockingStub(channel);
//
//        DRMSServiceAuthToken authToken = DRMSServiceAuthToken.newBuilder().
//                setAccessToken("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4bTBuMk91R1hLckxMREZ3S0lBQ2J4T2JMYzUtY0MzRnFkT3M2akdLaGQ4In0.eyJqdGkiOiIwNWI5ZWY0ZS1lYzRkLTQyMWUtOWQ4Zi1lNmQ0NzEyMjczMWEiLCJleHAiOjE2MjA3NDkzODEsIm5iZiI6MCwiaWF0IjoxNjIwNzQ3NTgxLCJpc3MiOiJodHRwczovL2tleWNsb2FrLmN1c3Rvcy5zY2lnYXAub3JnOjMxMDAwL2F1dGgvcmVhbG1zLzEwMDAwNzAyIiwiYXVkIjpbInJlYWxtLW1hbmFnZW1lbnQiLCJhY2NvdW50Il0sInN1YiI6ImU4NzE2NjkzLWE5MzYtNDcwNy1hYjhhLWM2ZGMxYjNiOTcxMSIsInR5cCI6IkJlYXJlciIsImF6cCI6ImN1c3Rvcy1jbWNkY2xieXdseG1jMmt0enYwZC0xMDAwMDcwMiIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjQ4ZjRmYWQyLWE1OTYtNGMzYy1hMTU5LWVkNWY3MjJkOTQ0ZiIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiaHR0cHM6Ly90ZXN0ZHJpdmUudXNlY3VzdG9zLm9yZyJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJhZG1pbiIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsicmVhbG0tbWFuYWdlbWVudCI6eyJyb2xlcyI6WyJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsInJlYWxtLWFkbWluIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInF1ZXJ5LXJlYWxtcyIsInZpZXctYXV0aG9yaXphdGlvbiIsInF1ZXJ5LWNsaWVudHMiLCJxdWVyeS11c2VycyIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIiwicXVlcnktZ3JvdXBzIl19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJDdXN0b3MgQWRtaW5EIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdHVzZXIiLCJnaXZlbl9uYW1lIjoiQ3VzdG9zIiwiZmFtaWx5X25hbWUiOiJBZG1pbkQiLCJlbWFpbCI6ImN1c3Rvcy1hZG1pbkBpdS5lZHUifQ.h4N2Xcg2n7Res6Y-t5OgGK_W1yRCWCUNoLnrCWM3mQRHcbT6Y0t9MwGMoFaA85rYtlLCh-LL9GN8nQ_TNhWgVcmGlY9UI7xaoGHwVyoHTuIqcy9Ojj-8FcAni0s7Ix_1PZ7OYWZLO8yVZDQCmR28lUWx644t8ENkuwix20yiXmWMyNP8xwBnfcQQWDcLTEk3N07dp5fqYKxFzTC13gAXl9HhRcnXrscHmAha-psLdGwz_yVsAhAjGRRBKbhqbLM4lL1YsWIWkHBIuUDbebrREYB6-xQgZEhLMMC2k2u6bDMPmOzhPYiY_V3S8f13jsyC-1NYFUekLAcoCOY-wx_soQ")
//                .build();
//        ResourceSearchRequest request = ResourceSearchRequest.newBuilder().setAuthToken(authToken).build();

        StorageServiceGrpc.StorageServiceBlockingStub resourceClient = StorageServiceGrpc.newBlockingStub(channel);

        DRMSServiceAuthToken authToken = DRMSServiceAuthToken.newBuilder().
                setAccessToken("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4bTBuMk91R1hLckxMREZ3S0lBQ2J4T2JMYzUtY0MzRnFkT3M2akdLaGQ4In0.eyJqdGkiOiJmYmQ4ZGI2ZS0yNTdkLTRjZTYtODlhYS0zN2JhZDg2NGFkOGYiLCJleHAiOjE2MjM2NDA3NjEsIm5iZiI6MCwiaWF0IjoxNjIzNjM4OTYxLCJpc3MiOiJodHRwczovL2tleWNsb2FrLmN1c3Rvcy5zY2lnYXAub3JnOjMxMDAwL2F1dGgvcmVhbG1zLzEwMDAwNzAyIiwiYXVkIjpbInJlYWxtLW1hbmFnZW1lbnQiLCJhY2NvdW50Il0sInN1YiI6ImU4NzE2NjkzLWE5MzYtNDcwNy1hYjhhLWM2ZGMxYjNiOTcxMSIsInR5cCI6IkJlYXJlciIsImF6cCI6ImN1c3Rvcy1jbWNkY2xieXdseG1jMmt0enYwZC0xMDAwMDcwMiIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjAzYTA2ZDc0LTcwNGUtNDUzZS05MTlhLTdiZTJiNTNkY2E2YSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cHM6Ly9nYXRld2F5Lml1YmVtY2VudGVyLmluZGlhbmEuZWR1IiwiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiaHR0cHM6Ly90ZXN0ZHJpdmUudXNlY3VzdG9zLm9yZyJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJhZG1pbiIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsicmVhbG0tbWFuYWdlbWVudCI6eyJyb2xlcyI6WyJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsInJlYWxtLWFkbWluIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInF1ZXJ5LXJlYWxtcyIsInZpZXctYXV0aG9yaXphdGlvbiIsInF1ZXJ5LWNsaWVudHMiLCJxdWVyeS11c2VycyIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIiwicXVlcnktZ3JvdXBzIl19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJDdXN0b3MgQWRtaW4iLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ0ZXN0dXNlciIsImdpdmVuX25hbWUiOiJDdXN0b3MiLCJmYW1pbHlfbmFtZSI6IkFkbWluIiwiZW1haWwiOiJjdXN0b3MtYWRtaW5AaXUuZWR1In0.ZpK4OKAJy3dGC3XpFzA2YDY9j-7nmNCMe7i9xoXFcId0jOLcp7v47px3YHbHepuNjM6pm56lAfHFBKCftV1AFEJt91YMf9q984FNiYhIyb6YhSOPoXT0CUJroTs6XPCC0O1ro4knd9SnchaXjLTUoxGWjPgiIT4-b5q0vtkoOwCPa4STegVMzBvv261nGrULoREaJxUiD1Xq6MNFKHo_Z_SrUhH8BYXUj4vY0w13KD6kiGr_jtwZm2_2BCJhDiRdPqRpf_O8MG0vB4nBOwreH11KVSXsF1q0goVlZ6WV7XxGmq8JTQJwANVxr7fZB9TCRVc98E9fBLL1Q3zNbInwZQ").build();
//        StorageCreateRequest request = StorageCreateRequest.newBuilder().setAuthToken(authToken).
//                setStorage(AnyStorage.newBuilder().setSshStorage(SSHStorage.newBuilder()
//                        .setStorageId("STORAGE_ONE_u6fniwcKcBZTP5v")
//                        .setHostName("localhost")
//                        .setPort(3565)
//                        .build())
//                        .build()).
//                build();
//
//        StorageCreateResponse response = resourceClient.createStorage(request);

//        StorageSearchRequest storageSearchRequest = StorageSearchRequest
//                .newBuilder()
//                .setAuthToken(authToken)
//                .addQueries(StorageSearchQuery.newBuilder().build())
//                .build();
//
//        resourceClient.searchStorage(storageSearchRequest);

        StoragePreferenceServiceGrpc.StoragePreferenceServiceBlockingStub storagePreferenceServiceBlockingStub = StoragePreferenceServiceGrpc
                .newBlockingStub(channel);
        StoragePreferenceSearchRequest storagePreferenceSearchRequest = StoragePreferenceSearchRequest
                .newBuilder()
                .setAuthToken(authToken)
                .addQueries(StoragePreferenceSearchQuery.newBuilder().build()).build();
        storagePreferenceServiceBlockingStub.searchStoragePreference(storagePreferenceSearchRequest);


    }
}
