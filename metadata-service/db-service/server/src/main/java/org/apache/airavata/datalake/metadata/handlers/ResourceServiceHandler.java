package org.apache.airavata.datalake.metadata.handlers;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators.ResourceServiceImpl;
import org.apache.airavata.datalake.metadata.parsers.ResourceParser;
import org.apache.airavata.datalake.metadata.service.*;
import org.dozer.DozerBeanMapper;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class ResourceServiceHandler extends ResourceMetadataServiceGrpc.ResourceMetadataServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceHandler.class);
    @Autowired
    private DozerBeanMapper dozerBeanMapper;

    @Autowired
    private ResourceParser resourceParser;

    @Autowired
    private Connector connector;


    @Override
    public void createResource(ResourceMetadataAPIRequest request,
                               StreamObserver<ResourceMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while creating tenant " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void getResource(ResourceMetadataAPIRequest request,
                            StreamObserver<Resource> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while fetching tenant " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void updateResource(ResourceMetadataAPIRequest request,
                               StreamObserver<ResourceMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while updating tenant " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void deleteResource(ResourceMetadataAPIRequest request,
                               StreamObserver<ResourceMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while deleting tenant " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void shareResource(ResourceMetadataSharingRequest request,
                              StreamObserver<ResourceMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while sharing resource " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void deleteSharing(ResourceMetadataSharingRequest request,
                              StreamObserver<ResourceMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {
            String msg = "Exception occurred while delete sharing " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void hasAccess(ResourcePermissionRequest request,
                          StreamObserver<ResourcePermissionResponse> responseObserver) {
        try {
            ResourceServiceImpl resourceService = new ResourceServiceImpl(connector);
            boolean accessible = resourceService.hasAccess(request.getUsername(),
                    request.getResourceName(), request.getPermissionType(),
                    request.getTenantId());

            ResourcePermissionResponse response = ResourcePermissionResponse
                    .newBuilder()
                    .setAccessible(accessible)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {

        }

    }
}
