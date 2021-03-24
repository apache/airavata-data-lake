package org.apache.airavata.datalake.metadata.handlers;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.metadata.service.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GRpcService
public class GroupServiceHandler extends GroupMetadataServiceGrpc.GroupMetadataServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceHandler.class);

    @Override
    public void createGroup(GroupMetadataAPIRequest request,
                            StreamObserver<GroupMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while creating group " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void getGroup(GroupMetadataAPIRequest request,
                         StreamObserver<Group> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while fetching group " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void updateGroup(GroupMetadataAPIRequest request,
                            StreamObserver<GroupMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while updating group " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void deleteGroup(GroupMetadataAPIRequest request,
                            StreamObserver<GroupMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while deleting group " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void createGroupMemberships(GroupMembershipAPIRequest request,
                                       StreamObserver<GroupMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while creating group memberships " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void deleteGroupMemberships(GroupMembershipAPIRequest request,
                                       StreamObserver<GroupMetadataAPIResponse> responseObserver) {
        try {





        } catch (Exception ex) {
            String msg = "Exception occurred while deleting group memberships " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }
}
