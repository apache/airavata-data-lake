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
import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.airavata.datalake.drms.storage.preference.ssh.SSHStoragePreference;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.identity.management.client.IdentityManagementClient;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

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


//        StorageCreateRequest request = StorageCreateRequest.newBuilder().setAuthToken(authToken).
//                setStorage(AnyStorage.newBuilder().setSshStorage(SSHStorage.newBuilder()
//                        .setStorageId("qwerft-rftgyhu-oplmnj")
//                        .setHostName("localhost")
//                        .setPort(3565)
//                        .build())
//                        .build()).
//                build();
//        resourceClient.createStorage(request);


        StorageSearchRequest storageSearchRequest = StorageSearchRequest
                .newBuilder()
                .setAuthToken(authToken)
                .addQueries(StorageSearchQuery.newBuilder()
                        .setField("type")
                        .setValue("COLLECTION")
                        .build())
                .build();

//        resourceClient.searchStorage(storageSearchRequest);

        StoragePreferenceServiceGrpc.StoragePreferenceServiceBlockingStub storagePreferenceServiceBlockingStub = StoragePreferenceServiceGrpc
                .newBlockingStub(channel);

//        StoragePreferenceSearchRequest storagePreferenceSearchRequest = StoragePreferenceSearchRequest
//                .newBuilder()
//                .setAuthToken(authToken)
//                .addQueries(StoragePreferenceSearchQuery.newBuilder().build()).build();
//        storagePreferenceServiceBlockingStub.searchStoragePreference(storagePreferenceSearchRequest);


//        StoragePreferenceFetchRequest storagePreferenceSearchRequest = StoragePreferenceFetchRequest
//                .newBuilder()
//                .setAuthToken(authToken)
//                .setStoragePreferenceId("storage-preference-id-one")
//                .build();
//      StoragePreferenceFetchResponse response =   storagePreferenceServiceBlockingStub
//              .fetchStoragePreference(storagePreferenceSearchRequest);
//        System.out.println(response.getStoragePreference().getSshStoragePreference().getUserName());


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

        TransferMapping transferMapping = TransferMapping.newBuilder()
                .setUserId("isjarana@iu.edu")
                .setTransferScope(TransferScope.GLOBAL)
                .setDestinationStoragePreference(AnyStoragePreference.newBuilder()
                        .setSshStoragePreference(SSHStoragePreference.newBuilder()
                                .setStoragePreferenceId("ssh_storage_preference").build()))
                .setSourceStoragePreference(AnyStoragePreference.newBuilder()
                        .setSshStoragePreference(SSHStoragePreference.newBuilder()
                                .setStoragePreferenceId("ssh_storage_preference_2").build()))
                .build();

        CreateTransferMappingRequest request = CreateTransferMappingRequest.newBuilder()
                .setAuthToken(authToken)
                .setTransferMapping(transferMapping)
                .build();

        FindTransferMappingsRequest findTransferMappingsRequest = FindTransferMappingsRequest.newBuilder()
                .setAuthToken(authToken)
                .build();

        DeleteTransferMappingRequest transferMappingRequest = DeleteTransferMappingRequest.newBuilder()
                .setAuthToken(authToken)
                .setId("ssh_storage_preference_2_ssh_storage_preference")
                .build();

//        storagePreferenceServiceBlockingStub.deleteTransferMappings(transferMappingRequest);

        storagePreferenceServiceBlockingStub.createTransferMapping(request);
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceServiceBlockingStub = ResourceServiceGrpc.newBlockingStub(channel);

//        ResourceSearchQuery query = ResourceSearchQuery.newBuilder().setField("type").setValue("COLLECTION").build();
//
//        ResourceSearchRequest request = ResourceSearchRequest
//                .newBuilder()
//                .setAuthToken(authToken)
//                .addQueries(query).build();
//
//        ResourceSearchResponse response =   resourceServiceBlockingStub.searchResource(request);
//        response.getResourcesList();

//        ChildResourceFetchRequest childResourceFetchRequest = ChildResourceFetchRequest
//                .newBuilder()
//                .setAuthToken(authToken)
//                .setResourceId("COLLECTION_TWO_a1qeBCVSrpx8vLJ")
//                .setDepth(1)
//                .setType("COLLECTION")
//                .build();
//
//        resourceServiceBlockingStub.fetchChildResources(childResourceFetchRequest);

        String id = UUID.randomUUID().toString();

        GenericResource genericResource = GenericResource
                .newBuilder()
                .setResourceId(id)
                .setType("COLLECTION")
                .setResourceName("COLLECTION_SDK_TEST_TWO")
                .build();


//        DRMSServiceAuthToken drmsServiceAuthToken = DRMSServiceAuthToken.newBuilder()
//                .setAccessToken(getServiceAccountToken())
//                .setAuthenticatedUser(AuthenticatedUser.newBuilder().setUsername("isjarana@iu.edu")
//                        .setTenantId("custos-whedmgamitu357p4wuke-10002708"))
//                .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
//                .build();
//        ResourceCreateRequest resourceCreateRequest = ResourceCreateRequest.newBuilder()
//                .setAuthToken(authToken)
//                .setResource(genericResource)
//                .build();
//        resourceServiceBlockingStub.createResource(resourceCreateRequest);


//        System.out.println(authToken.getAccessToken());
//        ResourceFetchRequest resourceFetchRequest = ResourceFetchRequest.newBuilder()
//                .setAuthToken(authToken)
//                .setResourceId("custos-whedmgamitu357p4wuke-10002708_29186.69999998808")
//                .build();
//
//        resourceServiceBlockingStub.fetchResource(resourceFetchRequest);
//
//        GenericResource parentResource = GenericResource.newBuilder()
//                .setResourceId("56cec8a2-a2c2-4669-9274-a5b5bdd97c11")
//                .setType("COLLECTION")
//                .build();
//
//        GenericResource childResource = GenericResource.newBuilder()
//                .setResourceId("b75b4cec-8df4-4f99-9d06-818db285cf02")
//                .setType("COLLECTION")
//                .build();
//
//        GenericResource childResource1 = GenericResource.newBuilder()
//                .setResourceId("b7ee2fd5-c4b8-4bb9-896b-4cb98d91ad24")
//                .setType("COLLECTION")
//                .build();
//
//        List<GenericResource> genericResourceList = new ArrayList<>();
//        genericResourceList.add(childResource);
//        genericResourceList.add(childResource1);

//        AddChildResourcesMembershipRequest addChildResourcesMembershipRequest = AddChildResourcesMembershipRequest
//                .newBuilder()
//                .setParentResource(parentResource)
//                .addAllChildResources(genericResourceList)
//                .setAuthToken(authToken)
//                .build();
//        resourceServiceBlockingStub.addChildMembership(addChildResourcesMembershipRequest);


//        DeleteChildResourcesMembershipRequest deleteChildResourcesMembershipRequest = DeleteChildResourcesMembershipRequest
//                .newBuilder()
//                .setParentResource(parentResource)
//                .addAllChildResources(genericResourceList)
//                .setAuthToken(authToken)
//                .build();
//        resourceServiceBlockingStub.deleteChildMembership(deleteChildResourcesMembershipRequest);

//        ParentResourcesFetchRequest parentResourcesFetchRequest = ParentResourcesFetchRequest
//                .newBuilder()
//                .setAuthToken(authToken)
//                .setResourceId("FILE_ONE_bxZPopxbnPaAEq5")
//                .setType("FILE")
//                .setDepth(2)
//                .build();
//        resourceServiceBlockingStub.fetchParentResources(parentResourcesFetchRequest);
        try {
            FileInputStream fileInputStream =
                    new FileInputStream(
                            "/Users/isururanawaka/Documents/Airavata_Repository/airavata-data-lake" +
                                    "/data-resource-management-service/drms-api/src/main/resources/sample.json");
            JSONTokener tokener = new JSONTokener(fileInputStream);
            JSONObject root = new JSONObject(tokener);
//
            Struct.Builder structBuilder = Struct.newBuilder();
            JsonFormat.parser().merge(root.toString(), structBuilder);

//            AddResourceMetadataRequest addResourceMetadataRequest = AddResourceMetadataRequest
//                    .newBuilder()
//                    .setMetadata(structBuilder.build())
//                    .setAuthToken(authToken)
//                    .setResourceId("custos-whedmgamitu357p4wuke-10002708_132068.39999997616")
//                    .setType("FILE")
//                    .build();
//
//            resourceServiceBlockingStub.addResourceMetadata(addResourceMetadataRequest);

//        FetchResourceMetadataRequest addResourceMetadataRequest = FetchResourceMetadataRequest
//                .newBuilder()
//                .setAuthToken(authToken)
//                .setResourceId("custos-whedmgamitu357p4wuke-10002708_132068.39999997616")
//                .setType("FILE")
//                .build();
//
//        resourceServiceBlockingStub.fetchResourceMetadata(addResourceMetadataRequest);


        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    private static String getAccessToken() {
        try {

            CustosClientProvider custosClientProvider = new CustosClientProvider.Builder().setServerHost("custos.scigap.org")
                    .setServerPort(31499)
                    .setClientId("custos-whedmgamitu357p4wuke-10002708")
                    .setClientSec("mrMdl86Ia1H94cikW7CvHoh7L0ASNXQVt2aRzSIj").build();

            IdentityManagementClient identityManagementClient = custosClientProvider.getIdentityManagementClient();
            Struct struct = identityManagementClient.getToken(null, null, "isjarana@iu.edu", "IJR@circ@1", null, "password");
            return struct.getFieldsMap().get("access_token").getStringValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String getServiceAccountToken() {
        try {

            return Base64.getEncoder().encodeToString(("file-listener-service-account" + ":" + "yyvQs3bKk2xp6W9YwuRQO3PvZrXruwRh0e0nR5kR")
                    .getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
