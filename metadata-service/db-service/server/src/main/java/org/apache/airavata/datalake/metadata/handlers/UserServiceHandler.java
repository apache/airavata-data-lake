package org.apache.airavata.datalake.metadata.handlers;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.metadata.service.User;
import org.apache.airavata.datalake.metadata.service.UserMetadataAPIRequest;
import org.apache.airavata.datalake.metadata.service.UserMetadataAPIResponse;
import org.apache.airavata.datalake.metadata.service.UserMetadataServiceGrpc;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GRpcService
public class UserServiceHandler extends UserMetadataServiceGrpc.UserMetadataServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceHandler.class);

    @Override
    public void createUser(UserMetadataAPIRequest request, StreamObserver<UserMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while creating user " + ex;
            LOGGER.error(msg);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());

        }
    }

    @Override
    public void getUser(UserMetadataAPIRequest request, StreamObserver<User> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while fetching user " + ex;
            LOGGER.error(msg);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void updateUser(UserMetadataAPIRequest request, StreamObserver<UserMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while updating user " + ex;
            LOGGER.error(msg);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void deleteUser(UserMetadataAPIRequest request, StreamObserver<UserMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while deleting user " + ex;
            LOGGER.error(msg);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }


}
