package org.apache.airavata.datalake.metadata.handlers;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.metadata.service.*;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class ResourceServiceHandler extends ResourceMetadataServiceGrpc.ResourceMetadataServiceImplBase {
    @Override
    public void createResource(ResourceMetadataAPIRequest request, StreamObserver<ResourceMetadataAPIResponse> responseObserver) {
        super.createResource(request, responseObserver);
    }

    @Override
    public void getResource(ResourceMetadataAPIRequest request, StreamObserver<Resource> responseObserver) {
        super.getResource(request, responseObserver);
    }

    @Override
    public void updateResource(ResourceMetadataAPIRequest request, StreamObserver<ResourceMetadataAPIResponse> responseObserver) {
        super.updateResource(request, responseObserver);
    }

    @Override
    public void deleteResource(ResourceMetadataAPIRequest request, StreamObserver<ResourceMetadataAPIResponse> responseObserver) {
        super.deleteResource(request, responseObserver);
    }

    @Override
    public void shareResource(ResourceMetadataSharingRequest request, StreamObserver<ResourceMetadataAPIResponse> responseObserver) {
        super.shareResource(request, responseObserver);
    }

    @Override
    public void deleteSharing(ResourceMetadataSharingRequest request, StreamObserver<ResourceMetadataAPIResponse> responseObserver) {
        super.deleteSharing(request, responseObserver);
    }
}
