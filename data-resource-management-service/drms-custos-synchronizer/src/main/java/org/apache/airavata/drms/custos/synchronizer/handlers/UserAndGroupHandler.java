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
        this.custosClientProvider = Utils.getCustosClientProvider();
    }

    public void mergeUserAndGroups(Configuration configuration) {
        try {
            LOGGER.debug("Merging groups for custos client with id " + configuration.getCustos().getCustosId());
            String[] clientIds = configuration.getCustos().getTenantsToBeSynced();
            UserManagementClient userManagementClient = this.custosClientProvider.getUserManagementClient();
            GroupManagementClient groupManagementClient = this.custosClientProvider.getGroupManagementClient();
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
                    String query = "Merge (u:User{username: '" + userProfile.getUsername() + "',"
                            + "custosClientId:'" + val + "'}" + ")"
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
                    String id = gr.getId().replaceAll("'", "");
                    String query = "Merge (u:Group{groupId: '" + id + "',"
                            + "custosClientId:'" + val + "'}" + ")"
                            + " SET u = $props return u ";
                    Map<String, Object> map = new HashMap<>();
                    map.put("description", gr.getDescription());
                    map.put("name", gr.getName());
                    map.put("groupId", id);
                    map.put("createdTime", gr.getCreatedTime());
                    map.put("lastModifiedTime", gr.getLastModifiedTime());
                    map.put("custosClientId", val);
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("props", map);
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
                    String id = gr.getId().replaceAll("'", "");
                    GetAllUserProfilesResponse userProfilesResponse = groupManagementClient.getAllChildUsers(val, gr.getId());
                    userProfilesResponse.getProfilesList().forEach(prof -> {
                        String memberShipType = prof.getMembershipType();
                        String userId = prof.getUsername();
                        mergeUserMemberShip(userId, id, val, memberShipType);
                    });
                    GetAllGroupsResponse getAllGroupsResponse = groupManagementClient.getAllChildGroups(val, gr.getId());
                    getAllGroupsResponse.getGroupsList().forEach(grMem -> {
                        String childId = gr.getId().replaceAll("'", "");
                        mergeGroupMemberShip(id, childId, val);
                    });
                });
            });
        } catch (Exception ex) {
            LOGGER.error("Error occurred while merging groups ", ex);
        }
    }

    private void mergeUserMemberShip(String username, String groupId, String custosClientId, String role) {
        String query = "MATCH (a:User), (b:Group) WHERE a.username = '" + username + "' AND a.custosClientId = '"
                + custosClientId + "' AND " + "b.groupId ='" + groupId + "' AND b.custosClientId ='" + custosClientId +
                "' MERGE (a)-[r:MEMBER_OF]->(b) SET r.role='" + role + "' RETURN a, b";
        try {
            this.neo4JConnector.runTransactionalQuery(query);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while merging UserGroupMembership ", ex);
        }

    }

    private void mergeGroupMemberShip(String parentGroupId, String childGroupId, String custosClientId) {
        String query = "MATCH (a:Group), (b:Group) WHERE a.groupId = '" + parentGroupId + "' AND a.custosClientId = '"
                + custosClientId + "' AND " + "b.groupId ='" + childGroupId + "' AND b.custosClientId ='"
                + custosClientId + "' MERGE (a)<-[r:CHILD_OF]-(b)  RETURN a, b";
        try {
            this.neo4JConnector.runTransactionalQuery(query);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while merging Group memberships ", ex);
        }

    }

}
