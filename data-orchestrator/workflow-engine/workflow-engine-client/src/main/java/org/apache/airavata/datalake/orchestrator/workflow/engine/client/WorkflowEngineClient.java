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

package org.apache.airavata.datalake.orchestrator.workflow.engine.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowInvocationRequest;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowMessage;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowServiceGrpc;

public class WorkflowEngineClient {

    public static WorkflowServiceGrpc.WorkflowServiceBlockingStub buildClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        return WorkflowServiceGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();
        WorkflowServiceGrpc.WorkflowServiceBlockingStub workflowServiceStub =
                WorkflowServiceGrpc.newBlockingStub(channel);
        WorkflowInvocationRequest workflowInvocationRequest = WorkflowInvocationRequest
                .newBuilder()
                .setMessage(WorkflowMessage.newBuilder().setMessageId("387bd8e9-58ce-4626-8347-90a8ab12376e "))
                .build();
        workflowServiceStub.invokeWorkflow(workflowInvocationRequest);
        System.out.println("Done");
    }
}
