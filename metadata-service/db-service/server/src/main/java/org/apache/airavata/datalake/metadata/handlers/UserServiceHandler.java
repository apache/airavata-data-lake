package org.apache.airavata.datalake.metadata.handlers;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.metadata.service.*;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class UserServiceHandler extends UserMetadataServiceGrpc.UserMetadataServiceImplBase {
    @Override
    public void createUser(UserMetadataAPIRequest request, StreamObserver<UserMetadataAPIResponse> responseObserver) {
        super.createUser(request, responseObserver);
    }

    @Override
    public void getUser(UserMetadataAPIRequest request, StreamObserver<User> responseObserver) {
        super.getUser(request, responseObserver);
    }

    @Override
    public void updateUser(UserMetadataAPIRequest request, StreamObserver<UserMetadataAPIResponse> responseObserver) {
        super.updateUser(request, responseObserver);
    }

    @Override
    public void deleteUser(UserMetadataAPIRequest request, StreamObserver<UserMetadataAPIResponse> responseObserver) {
        super.deleteUser(request, responseObserver);
    }

}
