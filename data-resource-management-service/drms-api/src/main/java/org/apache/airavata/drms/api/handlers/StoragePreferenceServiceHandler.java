package org.apache.airavata.drms.api.handlers;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.drms.storage.*;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class StoragePreferenceServiceHandler extends StoragePreferenceServiceGrpc.StoragePreferenceServiceImplBase {

    @Override
    public void fetchStoragePreference(StoragePreferenceFetchRequest request, StreamObserver<StoragePreferenceFetchResponse> responseObserver) {
        super.fetchStoragePreference(request, responseObserver);
    }

    @Override
    public void createStoragePreference(StoragePreferenceCreateRequest request, StreamObserver<StoragePreferenceCreateResponse> responseObserver) {
        super.createStoragePreference(request, responseObserver);
    }

    @Override
    public void updateStoragePreference(StoragePreferenceUpdateRequest request, StreamObserver<StoragePreferenceUpdateResponse> responseObserver) {
        super.updateStoragePreference(request, responseObserver);
    }

    @Override
    public void deletePreferenceStorage(StoragePreferenceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        super.deletePreferenceStorage(request, responseObserver);
    }

    @Override
    public void searchStoragePreference(StoragePreferenceSearchRequest request, StreamObserver<StoragePreferenceSearchResponse> responseObserver) {
        super.searchStoragePreference(request, responseObserver);
    }
}
