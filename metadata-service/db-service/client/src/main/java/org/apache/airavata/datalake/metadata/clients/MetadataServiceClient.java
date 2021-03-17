package org.apache.airavata.datalake.metadata.clients;

import io.grpc.ManagedChannel;
import org.apache.airavata.datalake.metadata.service.GroupMetadataServiceGrpc;
import org.apache.airavata.datalake.metadata.service.ResourceMetadataServiceGrpc;
import org.apache.airavata.datalake.metadata.service.TenantMetadataServiceGrpc;
import org.apache.airavata.datalake.metadata.service.UserMetadataServiceGrpc;

public class MetadataServiceClient {

    private ManagedChannel channel;

    MetadataServiceClient(ManagedChannel channel) {
        this.channel = channel;
    }


    public TenantMetadataServiceGrpc.TenantMetadataServiceBlockingStub tenant() {
        return TenantMetadataServiceGrpc.newBlockingStub(channel);
    }

    public GroupMetadataServiceGrpc.GroupMetadataServiceBlockingStub group() {
        return GroupMetadataServiceGrpc.newBlockingStub(channel);
    }

    public UserMetadataServiceGrpc.UserMetadataServiceBlockingStub user() {
        return UserMetadataServiceGrpc.newBlockingStub(channel);
    }

    public ResourceMetadataServiceGrpc.ResourceMetadataServiceBlockingStub resource() {
        return ResourceMetadataServiceGrpc.newBlockingStub(channel);
    }


}
