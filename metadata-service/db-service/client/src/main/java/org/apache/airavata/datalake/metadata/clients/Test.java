package org.apache.airavata.datalake.metadata.clients;

import org.apache.airavata.datalake.metadata.service.*;

public class Test {
    public static void main(String[] args) {

        MetadataServiceClient serviceClient = MetadataServiceClientBuilder.buildClient("localhost", 9090);

        TenantMetadataServiceGrpc.TenantMetadataServiceBlockingStub stub = serviceClient.tenant();

        Tenant tenant = Tenant.newBuilder()
                .setTenantId("asdcfvf")
                .setName("TenantA")
                .build();
        Group group = Group.newBuilder()
                .setName("g1")
                .build();
        Group group2 = Group.newBuilder()
                .setName("g3")
                .build();
        Group group1 = Group.newBuilder()
                .setName("g2")
                .addChildGroups(group2)
                .build();

        User user = User.newBuilder()
                .setUsername("TestingUserA")
                .setFirstName("Isuru")
                .setLastName("Ranawaka")
                .build();

        GroupMembership groupMemberships = GroupMembership
                .newBuilder()
                .setUser(user)
                .setMembershipType("ADMIN")
                .build();

        group1 = group1.toBuilder()
                .addGroupMembership(groupMemberships)
                .build();
        group = group.toBuilder()
                .addChildGroups(group1)
                .build();

        Resource resource = Resource.newBuilder()
                .setName("R1")
                .build();
        Resource resource1 = Resource.newBuilder()
                .setName("R2")
                .build();
        Resource resource2 = Resource.newBuilder()
                .setName("R3")
                .build();

        resource1 = resource1.toBuilder()
                .addChildResources(resource2)
                .build();
        resource = resource.toBuilder()
                .addChildResources(resource1)
                .build();

        tenant = tenant.toBuilder()
                .addGroups(group)
                .build();
        tenant = tenant.toBuilder()
                .addResources(resource)
                .build();

        TenantMetadataAPIRequest request = TenantMetadataAPIRequest
                .newBuilder()
                .setTenant(tenant)
                .build();

        stub.createTenant(request);

    }
}
