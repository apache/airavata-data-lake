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

package org.apache.airavata.datalake.workflow.engine.wm.datasync;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.orchestrator.workflow.engine.*;
import org.apache.airavata.datalake.workflow.engine.wm.datasync.mft.MFTCallbackEntity;
import org.apache.airavata.datalake.workflow.engine.wm.datasync.mft.MFTCallbackStore;
import org.dozer.DozerBeanMapper;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@GRpcService
public class WorkflowEngineAPIHandler extends WorkflowServiceGrpc.WorkflowServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngineAPIHandler.class);

    @Autowired
    private MFTCallbackStore mftCallbackStore;

    @Autowired
    private DataSyncWorkflowManager dataSyncWorkflowManager;

    @Autowired
    private DataParsingWorkflowManager dataParsingWorkflowManager;

    @Override
    public void invokeWorkflow(WorkflowInvocationRequest request,
                               StreamObserver<WorkflowInvocationResponse> responseObserver) {
        try {
            logger.info("Invoking workflow executor for resource {}", request.getMessage().getSourceResourceId());
            //dataSyncWorkflowManager.submitDataSyncWorkflow(request);
            dataParsingWorkflowManager.submitDataParsingWorkflow(request);
            responseObserver.onNext(WorkflowInvocationResponse.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Error occurred while invoking  pipeline";
            logger.error(msg, ex);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void getMFTCallback(MFTCallbackGetRequest request, StreamObserver<MFTCallbackGetResponse> responseObserver) {
        DozerBeanMapper mapper = new DozerBeanMapper();
        Optional<MFTCallbackEntity> mftCallbackOp = mftCallbackStore.findMFTCallback(request.getMftTransferId());
        if (mftCallbackOp.isPresent()) {
            MFTCallbackEntity callbackEntity = mftCallbackOp.get();
            MFTCallbackGetResponse.Builder builder = MFTCallbackGetResponse.newBuilder();
            mapper.map(callbackEntity, builder);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } else {
            logger.error("No callback entity for mft transfer id {}", request.getMftTransferId());
            responseObserver.onError(new Exception("No callback entity for mft transfer id " + request.getMftTransferId()));
        }
    }

    @Override
    public void saveMFTCallback(MFTCallbacSaveRequest request, StreamObserver<Empty> responseObserver) {
        DozerBeanMapper mapper = new DozerBeanMapper();
        MFTCallbackEntity entity = mapper.map(request, MFTCallbackEntity.class);
        mftCallbackStore.saveMFTCallBack(entity);
        logger.info("Saved callback entity for transfer id {}", request.getMftTransferId());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
