package org.apache.airavata.drms.custos.synchronizer.handlers.events;

import org.apache.airavata.drms.custos.synchronizer.handlers.SharingHandler;
import org.apache.airavata.drms.custos.synchronizer.handlers.UserAndGroupHandler;
import org.apache.custos.messaging.service.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decode events coming from custos and invoke relevant handler
 */
public class EventDemux {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventDemux.class);


    public static void delegateEvents(Message message) {
        try {
            switch (message.getServiceName()) {
                case "USER_MANAGEMENT_SERVICE":
                    switch (message.getEventType()) {
                        case "DELETE_USER":
                    }
                    break;
                case "GROUP_MANAGEMENT_SERVICE":
                    switch (message.getEventType()) {
                        case "DELETE_GROUP":
                            deleteGroup(message);
                            break;


                    }
                    break;
                case "SHARING_MANAGEMENT_SERVICE":
                    switch (message.getEventType()) {
                        case "DELETE_ENTITY":
                            deleteEntity(message);
                            break;
                        case "REVOKE_ENTITY_SHARING_FROM_USERS":
                            deleteEntitySharingsForUsers(message);
                            break;
                        case "REVOKE_ENTITY_SHARING_FROM_GROUPS":
                            deleteEntitySharingsForGroups(message);
                            break;
                    }
                    break;
                default:
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    private static void deleteEntity(Message message) {
        SharingHandler sharingHandler = new SharingHandler();
        String clientId = message.getClientId();
        String entityId = message.getPropertiesMap().get("ENTITY_ID");
        String entityType = message.getPropertiesMap().get("ENTITY_TYPE");
        sharingHandler.deleteEntity(entityId, entityType, clientId);
    }

    private static void deleteEntitySharingsForUsers(Message message) {
        SharingHandler sharingHandler = new SharingHandler();
        String clientId = message.getClientId();
        String entityId = message.getPropertiesMap().get("ENTITY_ID");
        String entityType = message.getPropertiesMap().get("ENTITY_TYPE");
        String userId = message.getPropertiesMap().get("USER_ID");
        String permission = message.getPropertiesMap().get("PERMISSION_TYPE");
        sharingHandler.deleteEntitySharings(entityId, entityType, "USER", userId, permission, clientId);
    }

    private static void deleteEntitySharingsForGroups(Message message) {
        SharingHandler sharingHandler = new SharingHandler();
        String clientId = message.getClientId();
        String entityId = message.getPropertiesMap().get("ENTITY_ID");
        String entityType = message.getPropertiesMap().get("ENTITY_TYPE");
        String userId = message.getPropertiesMap().get("USER_ID");
        String permission = message.getPropertiesMap().get("PERMISSION_TYPE");
        sharingHandler.deleteEntity(entityId, entityType, clientId);
        sharingHandler.deleteEntitySharings(entityId, entityType, "GROUP", userId, permission, clientId);
    }

    private static void deleteGroup(Message message) {
        UserAndGroupHandler userAndGroupHandler = new UserAndGroupHandler();
        String clientId = message.getClientId();
        String groupId = message.getPropertiesMap().get("GROUP_ID");
        userAndGroupHandler.deleteGroup(groupId, clientId);
    }

    private static void deleteGroupMembership(Message message) {
        UserAndGroupHandler userAndGroupHandler = new UserAndGroupHandler();
        String clientId = message.getClientId();
        String groupId = message.getPropertiesMap().get("GROUP_ID");
        userAndGroupHandler.deleteGroup(groupId, clientId);
    }

}
