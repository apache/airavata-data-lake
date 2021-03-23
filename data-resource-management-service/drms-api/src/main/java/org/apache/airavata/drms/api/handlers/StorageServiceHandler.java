package org.apache.airavata.drms.api.handlers;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.drms.storage.*;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class StorageServiceHandler extends StorageServiceGrpc.StorageServiceImplBase {

    @Override
    public void fetchStorage(StorageFetchRequest request, StreamObserver<StorageFetchResponse> responseObserver) {
        super.fetchStorage(request, responseObserver);
    }

    @Override
    public void createStorage(StorageCreateRequest request, StreamObserver<StorageCreateResponse> responseObserver) {
        super.createStorage(request, responseObserver);
    }

    @Override
    public void updateStorage(StorageUpdateRequest request, StreamObserver<StorageUpdateResponse> responseObserver) {
        super.updateStorage(request, responseObserver);
    }

    @Override
    public void deleteStorage(StorageDeleteRequest request, StreamObserver<Empty> responseObserver) {
        super.deleteStorage(request, responseObserver);
    }

    @Override
    public void searchStorage(StorageSearchRequest request, StreamObserver<StorageSearchResponse> responseObserver) {
        super.searchStorage(request, responseObserver);
    }
}
