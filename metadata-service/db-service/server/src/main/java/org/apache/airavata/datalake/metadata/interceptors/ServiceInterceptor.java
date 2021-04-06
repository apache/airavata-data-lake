package org.apache.airavata.datalake.metadata.interceptors;

import io.grpc.Metadata;

public interface ServiceInterceptor {
    public <ReqT> ReqT intercept(String method, Metadata headers, ReqT msg) throws Exception;
}
