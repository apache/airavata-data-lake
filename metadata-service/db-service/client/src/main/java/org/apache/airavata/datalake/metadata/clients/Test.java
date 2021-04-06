package org.apache.airavata.datalake.metadata.clients;

import org.apache.airavata.datalake.metadata.service.*;

public class Test {
    public static void main(String[] args) {

        MetadataServiceClient serviceClient = MetadataServiceClientBuilder.buildClient("localhost", 9090);

        TenantMetadataServiceGrpc.TenantMetadataServiceBlockingStub stub = serviceClient.tenant();


        Tenant tenant = Tenant.newBuilder()
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .setName("Custos tutorial demo gateway hosted version")
                .build();

        Group group = Group.newBuilder()
                .setName("Jackie's Lab")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        Group group2 = Group.newBuilder()
                .setName("Jackson's Lab")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        Group group3 = Group.newBuilder()
                .setName("Jack's Lab")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        Group group4 = Group.newBuilder()
                .setName("Suresh's Lab")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        Group group5 = Group.newBuilder()
                .setName("Dinuka's Lab")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        Group group6 = Group.newBuilder()
                .setName("Sanjiva's Lab")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        Group group7 = Group.newBuilder()
                .setName("Thomas's Lab")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        Group group8 = Group.newBuilder()
                .setName("Jane's Lab")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        Group group9 = Group.newBuilder()
                .setName("Read Only Admin")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        Group group10 = Group.newBuilder()
                .setName("Admin")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        User user = User.newBuilder()
                .setUsername("testuser")
                .setFirstName("Custos")
                .setLastName("AdminD")
                .setEmailAddress("custos-admin@iu.edu")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        User user1 = User.newBuilder()
                .setUsername("sophia")
                .setFirstName("Sophia")
                .setLastName("Aron")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        User user2 = User.newBuilder()
                .setUsername("l.dinukadesilva@gmail.com")
                .setFirstName("Dinuka")
                .setLastName("DeSilva")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        User user3 = User.newBuilder()
                .setUsername("audrey")
                .setFirstName("Audrey")
                .setLastName("Aron")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .setEmailAddress("audrey@gmail.com")
                .build();

        User user4 = User.newBuilder()
                .setUsername("alice")
                .setFirstName("Alice")
                .setLastName("Aron")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .setEmailAddress("alice@gmail.com")
                .build();

        User user5 = User.newBuilder()
                .setUsername("adalee")
                .setFirstName("Adalee")
                .setLastName("Aron")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .setEmailAddress("adalee@gmail.com")
                .build();

        User user6 = User.newBuilder()
                .setUsername("abigaill")
                .setFirstName("Abigaill")
                .setLastName("Aron")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .setEmailAddress("abigaill@gmail.com")
                .build();

        User user7 = User.newBuilder()
                .setUsername("abelota")
                .setFirstName("Abelota")
                .setLastName("Aron")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .setEmailAddress("abelota@gmail.com")
                .build();

        GroupMembership groupMemberships = GroupMembership
                .newBuilder()
                .setUser(user)
                .setMembershipType("OWNER")
                .build();
        GroupMembership groupMembership1 = GroupMembership
                .newBuilder()
                .setUser(user3)
                .setMembershipType("MEMBER")
                .build();
        GroupMembership groupMembership2 = GroupMembership
                .newBuilder()
                .setUser(user4)
                .setMembershipType("MEMBER")
                .build();
        GroupMembership groupMembership3 = GroupMembership
                .newBuilder()
                .setUser(user7)
                .setMembershipType("MEMBER")
                .build();

        group10 = group10.toBuilder()
                .addGroupMembership(groupMemberships)
                .addGroupMembership(groupMembership1)
                .addGroupMembership(groupMembership2)
                .addGroupMembership(groupMembership3)
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        GroupMembership groupMembership4 = GroupMembership
                .newBuilder()
                .setUser(user)
                .setMembershipType("OWNER")
                .build();
        GroupMembership groupMembership5 = GroupMembership
                .newBuilder()
                .setUser(user7)
                .setMembershipType("MEMBER")
                .build();

        group9 = group9.toBuilder()
                .addGroupMembership(groupMembership4)
                .addGroupMembership(groupMembership5)
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();


        group9 = group9.toBuilder()
                .addChildGroups(group10)
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        GroupMembership groupMembership6 = GroupMembership
                .newBuilder()
                .setUser(user6)
                .setMembershipType("OWNER")
                .build();
        GroupMembership groupMembership7 = GroupMembership
                .newBuilder()
                .setUser(user)
                .setMembershipType("MEMBER")
                .build();


        group8 = group8.toBuilder()
                .addGroupMembership(groupMembership6)
                .addGroupMembership(groupMembership7)
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();


        GroupMembership groupMembership9 = GroupMembership
                .newBuilder()
                .setUser(user)
                .setMembershipType("OWNER")
                .build();
        GroupMembership groupMembership10 = GroupMembership
                .newBuilder()
                .setUser(user6)
                .setMembershipType("MEMBER")
                .build();

        GroupMembership groupMembership11 = GroupMembership
                .newBuilder()
                .setUser(user7)
                .setMembershipType("MEMBER")
                .build();

        group7 = group7.toBuilder()
                .addGroupMembership(groupMembership9)
                .addGroupMembership(groupMembership10)
                .addGroupMembership(groupMembership11)
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        GroupMembership groupMembership12 = GroupMembership
                .newBuilder()
                .setUser(user)
                .setMembershipType("OWNER")
                .build();

        GroupMembership groupMembership13 = GroupMembership
                .newBuilder()
                .setUser(user6)
                .setMembershipType("MEMBER")
                .build();

        group6 = group6.toBuilder()
                .addGroupMembership(groupMembership12)
                .addGroupMembership(groupMembership13)
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();


        Resource resource = Resource.newBuilder()
                .setName("FileA")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        Resource resource1 = Resource.newBuilder()
                .setName("FileB")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        Resource resource2 = Resource.newBuilder()
                .setName("FileC")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        resource1 = resource1.toBuilder()
                .addChildResources(resource2)
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();

        ResourceSharings resourceSharings = ResourceSharings
                .newBuilder()
                .setPermissionType("READ")
                .addGroups(group6)
                .build();

        resource1 = resource1.toBuilder()
                .addSharings(resourceSharings)
                .build();

        resource = resource.toBuilder()
                .addChildResources(resource1)
                .build();

        tenant = tenant.toBuilder()
                .addGroups(group)
                .addGroups(group2)
                .addGroups(group3)
                .addGroups(group4)
                .addGroups(group5)
                .addGroups(group6)
                .addGroups(group7)
                .addGroups(group9)
                .addGroups(group8)
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

        ResourceMetadataServiceGrpc.ResourceMetadataServiceBlockingStub resourceMetadataServiceBlockingStub = serviceClient.resource();

        ResourcePermissionRequest permissionRequest = ResourcePermissionRequest
                .newBuilder()
                .setPermissionType("READ")
                .setUsername("testuser")
                .setResourceName("FileA")
                .setTenantId("custos-cmcdclbywlxmc2ktzv0d-10000702")
                .build();
      ResourcePermissionResponse response =   resourceMetadataServiceBlockingStub.hasAccess(permissionRequest);
        System.out.println(response.getAccessible());


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
        stub.createTenant(tenantMetadataAPIRequest);

    }
}
