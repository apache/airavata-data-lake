package org.apache.airavata.datalake.metadata.clients;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class MetadataServiceClientBuilder {

    public static MetadataServiceClient buildClient(String hostName, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(hostName, port).usePlaintext().build();
        return new MetadataServiceClient(channel);
    }
}
