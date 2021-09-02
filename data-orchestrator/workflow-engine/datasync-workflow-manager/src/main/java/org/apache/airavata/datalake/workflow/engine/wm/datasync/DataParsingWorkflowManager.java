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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.data.orchestrator.api.stub.parsing.*;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowInvocationRequest;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowMessage;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.AbstractTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.OutPort;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl.GenericDataParsingTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl.MetadataPersistTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl.SyncLocalDataDownloadTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.types.StringMap;
import org.apache.airavata.datalake.orchestrator.workflow.engine.wm.CallbackWorkflowStore;
import org.apache.airavata.datalake.orchestrator.workflow.engine.wm.WorkflowOperator;
import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.FetchResourceMetadataRequest;
import org.apache.airavata.mft.api.service.FileMetadataResponse;
import org.apache.airavata.mft.api.service.MFTApiServiceGrpc;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.common.DelegateAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.script.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DataParsingWorkflowManager {
    private final static Logger logger = LoggerFactory.getLogger(DataParsingWorkflowManager.class);

    @org.springframework.beans.factory.annotation.Value("${cluster.name}")
    private String clusterName;

    @org.springframework.beans.factory.annotation.Value("${parsing.wm.name}")
    private String workflowManagerName;

    @org.springframework.beans.factory.annotation.Value("${zookeeper.connection}")
    private String zkAddress;

    @org.springframework.beans.factory.annotation.Value("${mft.host}")
    private String mftHost;

    @org.springframework.beans.factory.annotation.Value("${mft.port}")
    private int mftPort;

    @org.springframework.beans.factory.annotation.Value("${orch.host}")
    private String orchHost;

    @org.springframework.beans.factory.annotation.Value("${orch.port}")
    private int orchPort;

    @org.springframework.beans.factory.annotation.Value("${drms.host}")
    private String drmsHost;

    @org.springframework.beans.factory.annotation.Value("${drms.port}")
    private int drmsPort;

    @org.springframework.beans.factory.annotation.Value("${mft.clientId}")
    private String mftClientId;

    @org.springframework.beans.factory.annotation.Value("${mft.clientSecret}")
    private String mftClientSecret;


    @Autowired
    private CallbackWorkflowStore callbackWorkflowStore;

    private WorkflowOperator workflowOperator;

    public void init() throws Exception {
        workflowOperator = new WorkflowOperator();
        workflowOperator.init(clusterName, workflowManagerName, zkAddress, callbackWorkflowStore);
        logger.info("Successfully initialized Data Parsing Workflow Manager");
    }

    public void submitDataParsingWorkflow(WorkflowInvocationRequest request) throws Exception {

        WorkflowMessage workflowMessage = request.getMessage();

        for (String sourceResourceId : workflowMessage.getSourceResourceIdsList()) {
            logger.info("Processing parsing workflow for resource {}", sourceResourceId);

            MFTApiServiceGrpc.MFTApiServiceBlockingStub mftClient = MFTApiClient.buildClient(mftHost, mftPort);

            DelegateAuth delegateAuth = DelegateAuth.newBuilder()
                    .setUserId(workflowMessage.getUsername())
                    .setClientId(mftClientId)
                    .setClientSecret(mftClientSecret)
                    .putProperties("TENANT_ID", workflowMessage.getTenantId()).build();

            FileMetadataResponse metadata = mftClient.getFileResourceMetadata(FetchResourceMetadataRequest.newBuilder()
                    .setResourceType("SCP")
                    .setResourceId(sourceResourceId)
                    .setResourceToken(workflowMessage.getSourceCredentialToken())
                    .setMftAuthorizationToken(AuthToken.newBuilder().setDelegateAuth(delegateAuth).build()).build());

            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6566).usePlaintext().build();
            DataParserServiceGrpc.DataParserServiceBlockingStub parserClient = DataParserServiceGrpc.newBlockingStub(channel);

            ParsingJobListResponse parsingJobs = parserClient.listParsingJobs(ParsingJobListRequest.newBuilder().build());

            Map<String, StringMap> parserInputMappings = new HashMap<>();
            List<DataParsingJob> selectedPJs = parsingJobs.getParsersList().stream().filter(pj -> {
                List<DataParsingJobInput> pjis = pj.getDataParsingJobInputsList();

                boolean match = true;
                StringMap stringMap = new StringMap();
                for (DataParsingJobInput pji : pjis) {

                    ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
                    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                    bindings.put("polyglot.js.allowHostAccess", true);
                    bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) s -> true);
                    bindings.put("metadata", metadata);
                    try {
                        Boolean eval = (Boolean) engine.eval(pji.getSelectionQuery());
                        stringMap.put(pji.getDataParserInputInterfaceId(), "$DOWNLOAD_PATH");
                        match = match && eval;
                    } catch (ScriptException e) {
                        logger.error("Failed to evaluate parsing job {}", pj.getDataParsingJobId());
                        match = false;
                    }
                }

                if (match) {
                    parserInputMappings.put(pj.getParserId(), stringMap);
                }
                return match;
            }).collect(Collectors.toList());

            if (selectedPJs.isEmpty()) {
                logger.warn("No parsing jobs available for resource {} with path {}. So ignoring the workflow",
                        sourceResourceId, metadata.getResourcePath());
                continue;
            }

            Map<String, AbstractTask> taskMap = new HashMap<>();

            SyncLocalDataDownloadTask downloadTask = new SyncLocalDataDownloadTask();
            downloadTask.setTaskId("SLDT-" + UUID.randomUUID().toString());
            downloadTask.setMftClientId(mftClientId);
            downloadTask.setMftClientSecret(mftClientSecret);
            downloadTask.setUserId(workflowMessage.getUsername());
            downloadTask.setTenantId(workflowMessage.getTenantId());
            downloadTask.setMftHost(mftHost);
            downloadTask.setMftPort(mftPort);
            downloadTask.setSourceResourceId(sourceResourceId);
            downloadTask.setSourceCredToken(workflowMessage.getSourceCredentialToken());

            taskMap.put(downloadTask.getTaskId(), downloadTask);

            for(String parserId: parserInputMappings.keySet()) {

                GenericDataParsingTask dataParsingTask = new GenericDataParsingTask();
                dataParsingTask.setTaskId("DPT-" + UUID.randomUUID().toString());
                dataParsingTask.setParserId(parserId);
                dataParsingTask.setParserServiceHost(orchHost);
                dataParsingTask.setParserServicePort(orchPort);
                dataParsingTask.setInputMapping(parserInputMappings.get(parserId));
                taskMap.put(dataParsingTask.getTaskId(), dataParsingTask);

                OutPort outPort = new OutPort();
                outPort.setNextTaskId(dataParsingTask.getTaskId());
                downloadTask.addOutPort(outPort);

                DataParsingJob dataParsingJob = selectedPJs.stream().filter(pj -> pj.getParserId().equals(parserId)).findFirst().get();
                ParserFetchResponse parser = parserClient.fetchParser(ParserFetchRequest.newBuilder().setParserId(parserId).build());

                for (DataParserOutputInterface dataParserOutputInterface: parser.getParser().getOutputInterfacesList()) {

                    Optional<DataParsingJobOutput> dataParsingJobOutput = dataParsingJob.getDataParsingJobOutputsList().stream().filter(o ->
                            o.getDataParserOutputInterfaceId().equals(dataParserOutputInterface.getParserOutputInterfaceId()))
                            .findFirst();

                    if (dataParsingJobOutput.isPresent() && dataParsingJobOutput.get().getOutputType().equals("JSON")) {
                        MetadataPersistTask mpt = new MetadataPersistTask();
                        mpt.setTaskId("MPT-" + UUID.randomUUID().toString());
                        mpt.setDrmsHost(drmsHost);
                        mpt.setDrmsPort(drmsPort);
                        mpt.setTenant(workflowMessage.getTenantId());
                        mpt.setUser(workflowMessage.getUsername());
                        mpt.setServiceAccountKey(mftClientId);
                        mpt.setServiceAccountSecret(mftClientSecret);
                        mpt.setResourceId(sourceResourceId);
                        mpt.setJsonFile("$" + dataParsingTask.getTaskId() + "-" + dataParserOutputInterface.getOutputName());
                        OutPort dpOut = new OutPort();
                        dpOut.setNextTaskId(mpt.getTaskId());
                        dataParsingTask.addOutPort(dpOut);
                        taskMap.put(mpt.getTaskId(), mpt);
                    }
                }

            }

            String[] startTaskIds = {downloadTask.getTaskId()};
            String workflowId = workflowOperator.buildAndRunWorkflow(taskMap, startTaskIds);

            logger.info("Submitted workflow {} to parse resource {} with path {}", workflowId,
                    sourceResourceId, metadata.getResourcePath());
        }
    }
}
