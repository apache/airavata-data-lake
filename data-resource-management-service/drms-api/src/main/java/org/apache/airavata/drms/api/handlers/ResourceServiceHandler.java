package org.apache.airavata.drms.api.handlers;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.drms.storage.*;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class ResourceServiceHandler extends ResourceServiceGrpc.ResourceServiceImplBase {
    @Override
    public void fetchResource(ResourceFetchRequest request, StreamObserver<ResourceFetchResponse> responseObserver) {
        super.fetchResource(request, responseObserver);
    }

    @Override
    public void createResource(ResourceCreateRequest request, StreamObserver<ResourceCreateResponse> responseObserver) {
        super.createResource(request, responseObserver);
    }

    @Override
    public void updateResource(ResourceUpdateRequest request, StreamObserver<ResourceUpdateResponse> responseObserver) {
        super.updateResource(request, responseObserver);
    }

    @Override
    public void deletePreferenceStorage(ResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        super.deletePreferenceStorage(request, responseObserver);
    }

    @Override
    public void searchResource(ResourceSearchRequest request, StreamObserver<ResourceSearchResponse> responseObserver) {
        super.searchResource(request, responseObserver);
    }
}
