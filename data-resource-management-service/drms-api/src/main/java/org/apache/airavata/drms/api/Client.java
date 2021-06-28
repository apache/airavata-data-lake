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

import com.google.protobuf.Struct;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.storage.StoragePreferenceFetchRequest;
import org.apache.airavata.datalake.drms.storage.StoragePreferenceFetchResponse;
import org.apache.airavata.datalake.drms.storage.StoragePreferenceServiceGrpc;
import org.apache.airavata.datalake.drms.storage.StorageServiceGrpc;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.identity.management.client.IdentityManagementClient;

public class Client {
    public static void main(String ar[]) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 7070).usePlaintext().build();
//        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceGrpc.newBlockingStub(channel);
//
//        DRMSServiceAuthToken authToken = DRMSServiceAuthToken.newBuilder().
//                setAccessToken()
//                .build();
//        ResourceSearchRequest request = ResourceSearchRequest.newBuilder().setAuthToken(authToken).build();

        StorageServiceGrpc.StorageServiceBlockingStub resourceClient = StorageServiceGrpc.newBlockingStub(channel);

        String token = getAccessToken();

        DRMSServiceAuthToken authToken = DRMSServiceAuthToken.newBuilder().
                setAccessToken(token).build();

//
//        StorageCreateRequest request = StorageCreateRequest.newBuilder().setAuthToken(authToken).
//                setStorage(AnyStorage.newBuilder().setSshStorage(SSHStorage.newBuilder()
//                        .setStorageId("qwerft-rftgyhu-oplmnj")
//                        .setHostName("localhost")
//                        .setPort(3565)
//                        .build())
//                        .build()).
//                build();
//        resourceClient.createStorage(request);
//


//        StorageSearchRequest storageSearchRequest = StorageSearchRequest
//                .newBuilder()
//                .setAuthToken(authToken)
//                .addQueries(StorageSearchQuery.newBuilder().build())
//                .build();
//
//        resourceClient.searchStorage(storageSearchRequest);

        StoragePreferenceServiceGrpc.StoragePreferenceServiceBlockingStub storagePreferenceServiceBlockingStub = StoragePreferenceServiceGrpc
                .newBlockingStub(channel);

//        StoragePreferenceSearchRequest storagePreferenceSearchRequest = StoragePreferenceSearchRequest
//                .newBuilder()
//                .setAuthToken(authToken)
//                .addQueries(StoragePreferenceSearchQuery.newBuilder().build()).build();
//        storagePreferenceServiceBlockingStub.searchStoragePreference(storagePreferenceSearchRequest);


        StoragePreferenceFetchRequest storagePreferenceSearchRequest = StoragePreferenceFetchRequest
                .newBuilder()
                .setAuthToken(authToken)
                .setStoragePreferenceId("storage-preference-id-one")
                .build();
      StoragePreferenceFetchResponse response =   storagePreferenceServiceBlockingStub
              .fetchStoragePreference(storagePreferenceSearchRequest);
        System.out.println(response.getStoragePreference().getSshStoragePreference().getUserName());


//        StoragePreferenceCreateRequest storagePreferenceCreateRequest = StoragePreferenceCreateRequest
//                .newBuilder()
//                .setAuthToken(authToken)
//                .setStoragePreference(AnyStoragePreference.newBuilder()
//                        .setSshStoragePreference(SSHStoragePreference.newBuilder()
//                                .setStorage(SSHStorage
//                                        .newBuilder()
//                                        .setStorageId("test-storage-id-two")
//                                        .setHostName("localhost")
//                                        .setPort(3545).build()
//                                )
//                                .setStoragePreferenceId("storage-preference-id-two")
//                                .setUserName("test")
//                                .setCredentialToken("cred-token-3457-okmlp")
//                                .build())).build();
//
//        storagePreferenceServiceBlockingStub.createStoragePreference(storagePreferenceCreateRequest);


    }


    private static String getAccessToken(){
        try {

            CustosClientProvider custosClientProvider = new CustosClientProvider.Builder().setServerHost("custos.scigap.org")
                    .setServerPort(31499)
                    .setClientId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                    .setClientSec("1hBjD0poRNTdxwLzV0baY5mgUlogPalH9jCFz9ZG").build();

            IdentityManagementClient identityManagementClient = custosClientProvider.getIdentityManagementClient();
            Struct struct = identityManagementClient.getToken(null, null, "testuser", "testuser1234", null, "password");
            return struct.getFieldsMap().get("access_token").getStringValue();
        }catch (Exception ex){
ex.printStackTrace();
        }
       return null;
    }
}
