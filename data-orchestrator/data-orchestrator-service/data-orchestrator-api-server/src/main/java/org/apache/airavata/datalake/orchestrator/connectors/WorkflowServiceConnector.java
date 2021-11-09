package org.apache.airavata.datalake.orchestrator.connectors;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.core.connector.AbstractConnector;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowInvocationRequest;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowMessage;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Drms connector to call DRMS services
 */

public class WorkflowServiceConnector implements AbstractConnector<Configuration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DRMSConnector.class);

    private ManagedChannel workflowChannel;
    private WorkflowServiceGrpc.WorkflowServiceBlockingStub workflowServiceStub;


    public WorkflowServiceConnector(Configuration configuration) throws Exception {
        this.init(configuration);
    }

    @Override
    public void init(Configuration configuration) throws Exception {
        this.workflowChannel = ManagedChannelBuilder
                .forAddress(configuration.getOutboundEventProcessor().getWorkflowEngineHost(),
                        configuration.getOutboundEventProcessor().getWorkflowPort()).usePlaintext().build();
        this.workflowServiceStub = WorkflowServiceGrpc.newBlockingStub(workflowChannel);
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    public void invokeWorkflow(String authToken, String username, String tenantId, List<String> sourceResourceIds,
                               String sourceSecretId, String dstResourceId, String destinationSecretId) {
        try {
            WorkflowMessage workflowMessage = WorkflowMessage.newBuilder()
                    .addAllSourceResourceIds(sourceResourceIds)
                    .setDestinationResourceId(dstResourceId)
                    .setUsername(username)
                    .setTenantId(tenantId)
                    .setSourceCredentialToken(sourceSecretId)
                    .setDestinationCredentialToken(destinationSecretId)
                    .setAuthToken(authToken)
                    .build();
            WorkflowInvocationRequest workflowInvocationRequest = WorkflowInvocationRequest
                    .newBuilder().setMessage(workflowMessage).build();
            this.workflowServiceStub.invokeWorkflow(workflowInvocationRequest);
        } catch (Exception ex) {
            String msg = "Error occurred while invoking workflow engine " + ex.getMessage();
            throw new RuntimeException(msg, ex);
        }
    }
}
