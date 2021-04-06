package org.apache.airavata.datalake.metadata.interceptors;

import io.grpc.Metadata;
import org.apache.airavata.datalake.metadata.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Authenticator implements ServiceInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    @Override
    public <ReqT> ReqT intercept(String method, Metadata headers, ReqT msg) {

        return msg;
    }

}
