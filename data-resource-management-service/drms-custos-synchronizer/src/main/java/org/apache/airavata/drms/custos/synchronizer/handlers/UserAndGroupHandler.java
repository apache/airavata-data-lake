package org.apache.airavata.drms.custos.synchronizer.handlers;

import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.airavata.drms.custos.synchronizer.Configuration;
import org.apache.airavata.drms.custos.synchronizer.Utils;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.group.management.client.GroupManagementClient;
import org.apache.custos.user.management.client.UserManagementClient;
import org.apache.custos.user.profile.service.GetAllGroupsResponse;
import org.apache.custos.user.profile.service.GetAllUserProfilesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class UserAndGroupHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAndGroupHandler.class);

    private final Neo4JConnector neo4JConnector;
    private CustosClientProvider custosClientProvider;

    public UserAndGroupHandler() {
        this.neo4JConnector = Utils.getNeo4JConnector();
    }

    public void mergeUserAndGroups(Configuration configuration) {
        try {
            LOGGER.debug("Merging groups for custos client with id " + configuration.getCustos().getCustosId());
            String[] clientIds = configuration.getCustos().getTenantsToBeSynced();
            UserManagementClient userManagementClient = Utils.getUserManagementClient();
            GroupManagementClient groupManagementClient = Utils.getGroupManagementClient();
            mergeUsers(userManagementClient, clientIds);
            mergeGroups(groupManagementClient, clientIds);
            mergeUserAndGroupMemberships(groupManagementClient, userManagementClient, clientIds);
        } catch (Exception ex) {
            String msg = "Exception occurred while merging user" + ex.getMessage();
            LOGGER.error(msg, ex);
        }


    }

    private void mergeUsers(UserManagementClient userManagementClient, String[] clientIds) {
        try {
            Arrays.stream(clientIds).forEach(val -> {
                GetAllUserProfilesResponse response = userManagementClient.getAllUserProfiles(val);
                response.getProfilesList().forEach(userProfile -> {
                    String query = "Merge (u:User{username: $username,"
                            + "custosClientId: $custosClientId}" + ")"
                            + " SET u = $props return u ";
                    Map<String, Object> map = new HashMap<>();
                    map.put("firstName", userProfile.getFirstName());
                    map.put("name", userProfile.getUsername());
                    map.put("lastName", userProfile.getLastName());
                    map.put("email", userProfile.getEmail());
                    map.put("username", userProfile.getUsername());
                    map.put("custosClientId", val);
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("props", map);
                    parameters.put("username", userProfile.getUsername());
                    parameters.put("custosClientId", val);
                    this.neo4JConnector.runTransactionalQuery(parameters, query);
                });

            });


        } catch (Exception ex) {
            LOGGER.error("Error occurred while merging user ", ex);
        }
    }


    private void mergeGroups(GroupManagementClient groupManagementClient, String[] clientIds) {
        try {
            Arrays.stream(clientIds).forEach(val -> {
                GetAllGroupsResponse response = groupManagementClient.getAllGroups(val);
                response.getGroupsList().forEach(gr -> {
                    String query = "Merge (u:Group{groupId: $groupId,"
                            + "custosClientId: $custosClientId} )"
                            + " SET u = $props return u ";
                    Map<String, Object> map = new HashMap<>();
                    map.put("description", gr.getDescription());
                    map.put("name", gr.getName());
                    map.put("groupId", gr.getId());
                    map.put("createdTime", gr.getCreatedTime());
                    map.put("lastModifiedTime", gr.getLastModifiedTime());
                    map.put("custosClientId", val);
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("props", map);
                    parameters.put("groupId", gr.getId());
                    parameters.put("custosClientId", val);
                    try {
                        this.neo4JConnector.runTransactionalQuery(parameters, query);
                    } catch (Exception ex) {
                        LOGGER.error("Error occurred while merging groups ", ex);
                    }
                });
            });
        } catch (Exception ex) {
            LOGGER.error("Error occurred while merging groups ", ex);
        }
    }

    private void mergeUserAndGroupMemberships(GroupManagementClient groupManagementClient, UserManagementClient userManagementClient,
                                              String[] clientIds) {
        try {
            Arrays.stream(clientIds).forEach(val -> {
                GetAllGroupsResponse response = groupManagementClient.getAllGroups(val);
                response.getGroupsList().forEach(gr -> {
                    GetAllUserProfilesResponse userProfilesResponse = groupManagementClient.getAllChildUsers(val, gr.getId());
                    userProfilesResponse.getProfilesList().forEach(prof -> {
                        String memberShipType = prof.getMembershipType();
                        String userId = prof.getUsername();
                        mergeUserMemberShip(userId, gr.getId(), val, memberShipType);
                    });
                    GetAllGroupsResponse getAllGroupsResponse = groupManagementClient.getAllChildGroups(val, gr.getId());
                    getAllGroupsResponse.getGroupsList().forEach(grMem -> {
                        mergeGroupMemberShip(gr.getId(), grMem.getId(), val);
                    });
                });
            });
        } catch (Exception ex) {
            LOGGER.error("Error occurred while merging groups ", ex);
        }
    }

    private void mergeUserMemberShip(String username, String groupId, String custosClientId, String role) {
        String query = "MATCH (a:User), (b:Group) WHERE a.username = $username AND a.custosClientId = $custosClientId AND "
                + "b.groupId =$groupId AND b.custosClientId =$custosClientId MERGE (a)-[r:MEMBER_OF]->(b) " +
                "SET r.role=$role RETURN a, b";
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("role", role);
        map.put("groupId", groupId);
        map.put("custosClientId", custosClientId);
        try {
            this.neo4JConnector.runTransactionalQuery(map, query);
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error("Error occurred while merging UserGroupMembership ", ex);
        }

    }

    private void mergeGroupMemberShip(String parentGroupId, String childGroupId, String custosClientId) {
        String query = "MATCH (a:Group), (b:Group) WHERE a.groupId = $parentGroupId AND a.custosClientId = $custosClientId" +
                " AND " + "b.groupId = $childGroupId AND b.custosClientId = $custosClientId " +
                "MERGE (a)<-[r:CHILD_OF]-(b)  RETURN a, b";
        Map<String, Object> map = new HashMap<>();
        map.put("parentGroupId", parentGroupId);
        map.put("custosClientId", custosClientId);
        map.put("childGroupId", childGroupId);
        try {
            this.neo4JConnector.runTransactionalQuery(map, query);
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error("Error occurred while merging Group memberships ", ex);
        }

    }

    public void deleteUser(String username, String clientId) {
        String query = "Match (u:User{username: $username,"
                + "custosClientId: $custosClientId}" + ")"
                + " DETACH DELETE u";
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("custosClientId", clientId);
        try {
            this.neo4JConnector.runTransactionalQuery(map, query);
        } catch (Exception ex) {
            String msg = "Error occurred while deleting user ";
            LOGGER.error(msg, ex);
        }

    }

    public void deleteGroup(String groupId, String clientId) {
        String query = "Match (g:Group{groupId: $groupId,"
                + "custosClientId: $custosClientId})"
                + " DETACH DELETE g";
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("custosClientId", clientId);
        try {
            this.neo4JConnector.runTransactionalQuery(map, query);
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = "Error occurred while deleting group ";
            LOGGER.error(msg, ex);
        }

    }

    public void deleteUserGroupMembership(String username, String custosClientId, String groupId) {
        String query = "MATCH (a:User)-[r:MEMBER_OF]->(b:Group) WHERE a.username = $username AND a.custosClientId = $custosClientId " +
                "AND " + "b.groupId =$groupId AND b.custosClientId =$custosClientId Delete r";
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("groupId", groupId);
        map.put("custosClientId", custosClientId);
        try {
            this.neo4JConnector.runTransactionalQuery(map, query);
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = "Error occurred while deleting user group membership from user " +
                    "" + username + " in group " + groupId;
            LOGGER.error(msg, ex);
        }

    }

    public void deleteGroupMembership(String parentGroupId, String childGroupId, String custosClientId, String groupId) {
        String query = "MATCH (a:Group)<-[r:CHILD_OF]-(b:Group) WHERE a.groupId = $parentGroupId AND a.custosClientId = $custosClientId " +
                " AND " + "b.groupId =  $childGroupId  AND b.custosClientId = $custosClientId  Delete r";
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("parentGroupId", parentGroupId);
        map.put("childGroupId", childGroupId);
        map.put("custosClientId", custosClientId);
        try {
            this.neo4JConnector.runTransactionalQuery(map, query);
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = "Error occurred while deleting  group memberships from "
                    + parentGroupId + " to " + childGroupId;
            LOGGER.error(msg, ex);
        }

    }

}
