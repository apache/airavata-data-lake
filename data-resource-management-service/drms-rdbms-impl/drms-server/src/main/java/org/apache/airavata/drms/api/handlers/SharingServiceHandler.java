package org.apache.airavata.drms.api.handlers;


import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.sharing.*;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.sharing.management.client.SharingManagementClient;
import org.apache.custos.sharing.service.Entity;
import org.apache.custos.sharing.service.PermissionType;
import org.apache.custos.sharing.service.SharingRequest;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class SharingServiceHandler extends DRMSSharingServiceGrpc.DRMSSharingServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SharingServiceHandler.class);

    @Autowired
    private CustosClientProvider custosClientProvider;


    @Override
    public void shareEntityWithUser(ShareEntityWithUserRequest request, StreamObserver<Empty> responseObserver) {
        try {

            AuthenticatedUser authenticatedUser = request.getAuthToken().getAuthenticatedUser();
            String username = authenticatedUser.getUsername();
            String tenantId = authenticatedUser.getTenantId();

            try (SharingManagementClient sharingManagementClient = custosClientProvider.getSharingManagementClient()) {
                ;
                Entity entity = Entity.newBuilder().setId(request.getEntityId()).build();
                PermissionType permissionTypeEditor = PermissionType.newBuilder().setId(request.getPermissionId()).build();
                PermissionType permissionTypeAdmin = PermissionType.newBuilder().setId("ADMIN").build();

                SharingRequest sharingRequestEditor = SharingRequest
                        .newBuilder()
                        .setClientId(tenantId)
                        .setEntity(entity)
                        .setPermissionType(permissionTypeEditor)
                        .addOwnerId(username).build();
                org.apache.custos.sharing.service.Status status = sharingManagementClient
                        .userHasAccess(tenantId, sharingRequestEditor);
                SharingRequest sharingRequestAdmin = SharingRequest
                        .newBuilder()
                        .setClientId(tenantId)
                        .setEntity(entity)
                        .setPermissionType(permissionTypeAdmin)
                        .addOwnerId(username).build();
                org.apache.custos.sharing.service.Status statusAdmin = sharingManagementClient
                        .userHasAccess(tenantId, sharingRequestAdmin);
                if (status.getStatus() || statusAdmin.getStatus()) {
                    SharingRequest shrRequest = SharingRequest
                            .newBuilder()
                            .setClientId(tenantId)
                            .setEntity(entity)
                            .setPermissionType(PermissionType.newBuilder().setId(request.getPermissionId()).build())
                            .addOwnerId(request.getSharedUserId()).build();
                    sharingManagementClient.shareEntityWithUsers(tenantId, shrRequest);
                    responseObserver.onNext(Empty.newBuilder().build());
                    responseObserver.onCompleted();

                } else {
                    String msg = "You don't have permission to manage sharing";
                    LOGGER.error(msg);
                    responseObserver.onError(Status.PERMISSION_DENIED.withDescription(msg).asRuntimeException());
                }
            }

        } catch (Exception ex) {
            LOGGER.error("Error occurred while sharing entity with user {}", request.getSharedUserId());
            String msg = "Error occurred while sharing entity with user {}" + request.getSharedUserId();
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }

    }

    @Override
    public void shareEntityWithGroup(ShareEntityWithGroupRequest request, StreamObserver<Empty> responseObserver) {
        try {

            AuthenticatedUser authenticatedUser = request.getAuthToken().getAuthenticatedUser();
            String username = authenticatedUser.getUsername();
            String tenantId = authenticatedUser.getTenantId();

            try (SharingManagementClient sharingManagementClient = custosClientProvider.getSharingManagementClient()) {
                Entity entity = Entity.newBuilder().setId(request.getEntityId()).build();
                PermissionType permissionTypeEditor = PermissionType.newBuilder().setId(request.getPermissionId()).build();
                PermissionType permissionTypeAdmin = PermissionType.newBuilder().setId("ADMIN").build();

                SharingRequest sharingRequestEditor = SharingRequest
                        .newBuilder()
                        .setClientId(tenantId)
                        .setEntity(entity)
                        .setPermissionType(permissionTypeEditor)
                        .addOwnerId(username).build();
                org.apache.custos.sharing.service.Status status = sharingManagementClient
                        .userHasAccess(tenantId, sharingRequestEditor);
                SharingRequest sharingRequestAdmin = SharingRequest
                        .newBuilder()
                        .setClientId(tenantId)
                        .setEntity(entity)
                        .setPermissionType(permissionTypeAdmin)
                        .addOwnerId(username).build();
                org.apache.custos.sharing.service.Status statusAdmin = sharingManagementClient
                        .userHasAccess(tenantId, sharingRequestAdmin);
                if (status.getStatus() || statusAdmin.getStatus()) {
                    SharingRequest shrRequest = SharingRequest
                            .newBuilder()
                            .setClientId(tenantId)
                            .setEntity(entity)
                            .setPermissionType(PermissionType.newBuilder().setId(request.getPermissionId()).build())
                            .addOwnerId(request.getSharedGroupId()).build();
                    sharingManagementClient.shareEntityWithGroups(tenantId, shrRequest);
                    responseObserver.onNext(Empty.newBuilder().build());
                    responseObserver.onCompleted();

                } else {
                    String msg = "You don't have permission to manage sharing";
                    LOGGER.error(msg);
                    responseObserver.onError(Status.PERMISSION_DENIED.withDescription(msg).asRuntimeException());
                }
            }

        } catch (Exception ex) {
            LOGGER.error("Error occurred while sharing entity with group {}", request.getSharedGroupId());
            String msg = "Error occurred while sharing entity with group {}" + request.getSharedGroupId();
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void revokeEntitySharingFromUser(RevokeEntityWithUserRequest request, StreamObserver<Empty> responseObserver) {
        try {
            AuthenticatedUser authenticatedUser = request.getAuthToken().getAuthenticatedUser();
            String username = authenticatedUser.getUsername();
            String tenantId = authenticatedUser.getTenantId();

            try (SharingManagementClient sharingManagementClient = custosClientProvider.getSharingManagementClient()) {
                Entity entity = Entity.newBuilder().setId(request.getEntityId()).build();
                PermissionType permissionTypeEditor = PermissionType.newBuilder().setId("EDITOR").build();
                PermissionType permissionTypeAdmin = PermissionType.newBuilder().setId("ADMIN").build();

                SharingRequest sharingRequestEditor = SharingRequest
                        .newBuilder()
                        .setClientId(tenantId)
                        .setEntity(entity)
                        .setPermissionType(permissionTypeEditor)
                        .addOwnerId(username).build();
                org.apache.custos.sharing.service.Status status = sharingManagementClient
                        .userHasAccess(tenantId, sharingRequestEditor);
                SharingRequest sharingRequestAdmin = SharingRequest
                        .newBuilder()
                        .setClientId(tenantId)
                        .setEntity(entity)
                        .setPermissionType(permissionTypeAdmin)
                        .addOwnerId(username).build();
                org.apache.custos.sharing.service.Status statusAdmin = sharingManagementClient
                        .userHasAccess(tenantId, sharingRequestAdmin);
                if (status.getStatus() || statusAdmin.getStatus()) {
                    SharingRequest shrRequest = SharingRequest
                            .newBuilder()
                            .setClientId(tenantId)
                            .setEntity(entity)
                            .setPermissionType(PermissionType.newBuilder().setId(request.getPermissionId()).build())
                            .addOwnerId(request.getRevokedUserId()).build();
                    sharingManagementClient.revokeEntitySharingFromUsers(tenantId, shrRequest);
                    responseObserver.onNext(Empty.newBuilder().build());
                    responseObserver.onCompleted();

                } else {
                    String msg = "You don't have permission to manage sharing";
                    LOGGER.error(msg);
                    responseObserver.onError(Status.PERMISSION_DENIED.withDescription(msg).asRuntimeException());
                }

            }
        } catch (Exception ex) {
            LOGGER.error("Error occurred while revoking entity with user {}", request.getRevokedUserId());
            String msg = "Error occurred while revoking entity with user {}" + request.getRevokedUserId();
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void revokeEntitySharingFromGroup(RevokeEntityFromGroupRequest request, StreamObserver<Empty> responseObserver) {
        try {
            AuthenticatedUser authenticatedUser = request.getAuthToken().getAuthenticatedUser();
            String username = authenticatedUser.getUsername();
            String tenantId = authenticatedUser.getTenantId();

            try (SharingManagementClient sharingManagementClient = custosClientProvider.getSharingManagementClient()) {
                Entity entity = Entity.newBuilder().setId(request.getEntityId()).build();
                PermissionType permissionTypeEditor = PermissionType.newBuilder().setId("EDITOR").build();
                PermissionType permissionTypeAdmin = PermissionType.newBuilder().setId("ADMIN").build();

                SharingRequest sharingRequestEditor = SharingRequest
                        .newBuilder()
                        .setClientId(tenantId)
                        .setEntity(entity)
                        .setPermissionType(permissionTypeEditor)
                        .addOwnerId(username).build();
                org.apache.custos.sharing.service.Status status = sharingManagementClient
                        .userHasAccess(tenantId, sharingRequestEditor);
                SharingRequest sharingRequestAdmin = SharingRequest
                        .newBuilder()
                        .setClientId(tenantId)
                        .setEntity(entity)
                        .setPermissionType(permissionTypeAdmin)
                        .addOwnerId(username).build();
                org.apache.custos.sharing.service.Status statusAdmin = sharingManagementClient
                        .userHasAccess(tenantId, sharingRequestAdmin);
                if (status.getStatus() || statusAdmin.getStatus()) {
                    SharingRequest shrRequest = SharingRequest
                            .newBuilder()
                            .setClientId(tenantId)
                            .setEntity(entity)
                            .setPermissionType(PermissionType.newBuilder().setId(request.getPermissionId()).build())
                            .addOwnerId(request.getRevokedGroupId()).build();
                    sharingManagementClient.revokeEntitySharingFromGroups(tenantId, shrRequest);
                    responseObserver.onNext(Empty.newBuilder().build());
                    responseObserver.onCompleted();

                } else {
                    String msg = "You don't have permission to manage sharing";
                    LOGGER.error(msg);
                    responseObserver.onError(Status.PERMISSION_DENIED.withDescription(msg).asRuntimeException());
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error occurred while revoking entity with user {}", request.getRevokedGroupId());
            String msg = "Error occurred while revoking entity with user {}" + request.getRevokedGroupId();
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }
}
