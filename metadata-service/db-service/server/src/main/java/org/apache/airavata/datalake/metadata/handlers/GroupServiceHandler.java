package org.apache.airavata.datalake.metadata.handlers;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.metadata.service.*;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class GroupServiceHandler extends GroupMetadataServiceGrpc.GroupMetadataServiceImplBase {
    @Override
    public void createGroup(GroupMetadataAPIRequest request, StreamObserver<GroupMetadataAPIResponse> responseObserver) {
        super.createGroup(request, responseObserver);
    }

    @Override
    public void getGroup(GroupMetadataAPIRequest request, StreamObserver<Group> responseObserver) {
        super.getGroup(request, responseObserver);
    }

    @Override
    public void updateGroup(GroupMetadataAPIRequest request, StreamObserver<GroupMetadataAPIResponse> responseObserver) {
        super.updateGroup(request, responseObserver);
    }

    @Override
    public void deleteGroup(GroupMetadataAPIRequest request, StreamObserver<GroupMetadataAPIResponse> responseObserver) {
        super.deleteGroup(request, responseObserver);
    }

    @Override
    public void createGroupMemberships(GroupMembershipAPIRequest request, StreamObserver<GroupMetadataAPIResponse> responseObserver) {
        super.createGroupMemberships(request, responseObserver);
    }

    @Override
    public void deleteGroupMemberships(GroupMembershipAPIRequest request, StreamObserver<GroupMetadataAPIResponse> responseObserver) {
        super.deleteGroupMemberships(request, responseObserver);
    }
}
