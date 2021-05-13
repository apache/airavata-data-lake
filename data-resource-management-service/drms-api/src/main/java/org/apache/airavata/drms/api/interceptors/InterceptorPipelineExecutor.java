package org.apache.airavata.drms.api.interceptors;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Stack;

/**
 * This class execute interceptor stack sequentially
 */

public class InterceptorPipelineExecutor implements ServerInterceptor {
    private final Logger LOGGER = LoggerFactory.getLogger(InterceptorPipelineExecutor.class);

    private Stack<ServiceInterceptor> interceptorSet;

    public InterceptorPipelineExecutor(Stack<ServiceInterceptor> integrationServiceInterceptors) {
        this.interceptorSet = integrationServiceInterceptors;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                                 Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String fullMethod = serverCall.getMethodDescriptor().getFullMethodName();
        String methodName = fullMethod.split("/")[1];
        String serviceName = fullMethod.split("/")[0];

        LOGGER.info("Calling method : " + serverCall.getMethodDescriptor().getFullMethodName());
        metadata.put(Metadata.Key.of("service-name", Metadata.ASCII_STRING_MARSHALLER), serviceName);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(serverCallHandler.startCall(serverCall, metadata)) {

            ReqT resp = null;

            @Override
            public void onMessage(ReqT message) {
                try {
                    Iterator it = interceptorSet.iterator();
                    while (it.hasNext()) {
                        ServiceInterceptor interceptor = (ServiceInterceptor) it.next();
                        resp = interceptor.intercept(methodName, metadata, (resp == null) ? message : resp);
                    }
                    super.onMessage(resp);
                } catch (Exception ex) {
                    String msg = "Error while validating method " + methodName + ", " + ex.getMessage();
                    LOGGER.error(msg, ex);
                    serverCall.close(Status.INVALID_ARGUMENT.withDescription(msg), new Metadata());
                }
            }

            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (IllegalStateException e) {
                    LOGGER.debug(e.getMessage());
                } catch (Exception e) {
                    String msg = "Error while validating method " + methodName + ", " + e.getMessage();
                    LOGGER.error(msg);
                    serverCall.close(Status.INVALID_ARGUMENT.withDescription(msg), metadata);
                }
            }

        };

    }
}
