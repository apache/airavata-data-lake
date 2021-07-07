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

package org.apache.airavata.datalake.mft.listener;

import org.apache.airavata.datalake.orchestrator.workflow.engine.MFTCallbackGetRequest;
import org.apache.airavata.datalake.orchestrator.workflow.engine.MFTCallbackGetResponse;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowServiceGrpc;
import org.apache.airavata.datalake.orchestrator.workflow.engine.client.WorkflowEngineClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/mft")
public class MFTFilter {

    private final static Logger logger = LoggerFactory.getLogger(MFTFilter.class);

    @Autowired
    private KafkaMFTMessageProducer mftMessageProducer;

    @org.springframework.beans.factory.annotation.Value("${datasync.wm.grpc.host}")
    private String datasyncWmHost;

    @org.springframework.beans.factory.annotation.Value("${datasync.wm.grpc.port}")
    private int datasyncWmPort;

    @GetMapping(value = "/{transferId}/{status}")
    public String fetchSFTPRemote(@PathVariable(name = "transferId") String transferId,
                                  @PathVariable(name = "status") String status) {

        logger.info("Got transfer status {} for transfer id {}", status, transferId);

        WorkflowServiceGrpc.WorkflowServiceBlockingStub wmClient = WorkflowEngineClient.buildClient(datasyncWmHost, datasyncWmPort);
        MFTCallbackGetResponse mftCallback = wmClient.getMFTCallback(
                MFTCallbackGetRequest.newBuilder().setMftTransferId(transferId).build());

        DataTransferEvent dataTransferEvent = new DataTransferEvent();
        dataTransferEvent.setTransferStatus(status);
        dataTransferEvent.setTaskId(mftCallback.getTaskId());
        dataTransferEvent.setWorkflowId(mftCallback.getWorkflowId());
        try {
            mftMessageProducer.publish(dataTransferEvent);
        } catch (Exception e) {
            logger.error("Failed to publish even to kafka with transfer id {} and status {}", transferId, status);
        }
        return "OK";
    }
}
