package org.apache.airavata.datalake.metadata.clients;

import org.apache.airavata.datalake.metadata.service.*;

public class Test {
    public static void main(String[] args) {

        MetadataServiceClient serviceClient = MetadataServiceClientBuilder.buildClient("localhost", 9090);

        TenantMetadataServiceGrpc.TenantMetadataServiceBlockingStub stub = serviceClient.tenant();


        Tenant tenant = Tenant.newBuilder()
                .setTenantId("100010402")
                .setName("TenantA")
                .build();

        Group group = Group.newBuilder()
                .setName("g1")
                .setTenantId("100010402")
                .build();

        Group group2 = Group.newBuilder()
                .setName("g3")
                .setTenantId("100010402")
                .build();

        User user = User.newBuilder()
                .setUsername("TestingUserA")
                .setFirstName("Isuru")
                .setLastName("Ranawaka")
                .setTenantId("100010402")
                .build();

        GroupMembership groupMemberships = GroupMembership
                .newBuilder()
                .setUser(user)
                .setMembershipType("ADMIN")
                .build();

        Group group1 = Group.newBuilder()
                .setName("g2")
                .setTenantId("100010402")
                .addChildGroups(group2)
                .build();

        group1 = group1.toBuilder()
                .addGroupMembership(groupMemberships)
                .setTenantId("100010402")
                .build();

        group = group.toBuilder()
                .addChildGroups(group1)
                .setTenantId("100010402")
                .build();

        Resource resource = Resource.newBuilder()
                .setName("R1")
                .setTenantId("100010402")
                .build();

        Resource resource1 = Resource.newBuilder()
                .setName("R2")
                .setTenantId("100010402")
                .build();

        Resource resource2 = Resource.newBuilder()
                .setName("R3")
                .setTenantId("100010402")
                .build();

        resource1 = resource1.toBuilder()
                .addChildResources(resource2)
                .setTenantId("100010402")
                .build();

        ResourceSharings resourceSharings = ResourceSharings
                .newBuilder()
                .setPermissionType("READ")
                .addGroups(group2)
                .build();

        resource1 = resource1.toBuilder()
                .addSharings(resourceSharings)
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

//        stub.createTenant(request);
//

//        ResourceMetadataServiceGrpc.ResourceMetadataServiceBlockingStub resourceMetadataServiceBlockingStub = serviceClient.resource();
//
//        ResourcePermissionRequest permissionRequest = ResourcePermissionRequest
//                .newBuilder()
//                .setPermissionType("READ")
//                .setUsername("TestingUserA")
//                .setResourceName("R5")
//                .setTenantId("100010402")
//                .build();
//      ResourcePermissionResponse response =   resourceMetadataServiceBlockingStub.hasAccess(permissionRequest);


//        TenantMetadataAPIRequest tenantMetadataAPIRequest = TenantMetadataAPIRequest
//                .newBuilder()
//                .setTenant(tenant)
//                .build();
//
//        stub.deleteTenant(tenantMetadataAPIRequest);

        tenant = tenant.toBuilder().setDomain("testing.com").build();


        TenantMetadataAPIRequest tenantMetadataAPIRequest = TenantMetadataAPIRequest
                .newBuilder()
                .setTenant(tenant)
                .build();
        stub.updateTenant(tenantMetadataAPIRequest);

    }
}
