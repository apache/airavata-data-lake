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

import org.apache.airavata.datalake.mft.listener.DataTransferEvent;
import org.apache.airavata.datalake.mft.listener.DataTransferEventDeserializer;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowInvocationRequest;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.AbstractTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.TaskUtil;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl.DataTransferPreValidationTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.wm.CallbackWorkflowEntity;
import org.apache.airavata.datalake.orchestrator.workflow.engine.wm.CallbackWorkflowStore;
import org.apache.airavata.datalake.orchestrator.workflow.engine.wm.WorkflowOperator;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class DataSyncWorkflowManager {

    private final static Logger logger = LoggerFactory.getLogger(DataSyncWorkflowManager.class);

    @org.springframework.beans.factory.annotation.Value("${cluster.name}")
    private String clusterName;

    @org.springframework.beans.factory.annotation.Value("${datasync.wm.name}")
    private String workflowManagerName;

    @org.springframework.beans.factory.annotation.Value("${zookeeper.connection}")
    private String zkAddress;

    @org.springframework.beans.factory.annotation.Value("${kafka.url}")
    private String kafkaUrl;

    @org.springframework.beans.factory.annotation.Value("${kafka.mft.status.consumer.group}")
    private String kafkaConsumerGroup;

    @org.springframework.beans.factory.annotation.Value("${kafka.mft.status.publish.topic}")
    private String kafkaTopic;

    @org.springframework.beans.factory.annotation.Value("${datasync.wm.grpc.host}")
    private String datasyncWmHost;

    @org.springframework.beans.factory.annotation.Value("${datasync.wm.grpc.port}")
    private int datasyncWmPort;


    @Value("${mft.callback.url}")
    private String callbackURL;
    @Value("${mft.host}")
    private String mftHost;
    @Value("${mft.port}")
    private int mftPort;
    @Value("${mft.clientId}")
    private String mftClientId;
    @Value("${mft.clientSecret}")
    private String mftClientSecret;

    @Value("${custos.host}")
    private String custosHost;

    @Value("${custos.port}")
    private int custosPort;

    @Value("${custos.id}")
    private String custosId;

    @Value("${custos.secret}")
    private String custosSecret;

    @Value("${drms.host}")
    private String drmsHost;

    @Value("${drms.port}")
    private int drmsPort;


    @Autowired
    private CallbackWorkflowStore callbackWorkflowStore;

    private final ExecutorService kafkaMessageProcessPool = Executors.newFixedThreadPool(10);

    private WorkflowOperator workflowOperator;

    private Consumer<String, DataTransferEvent> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DataTransferEventDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 20);
        // Create the consumer using props.
        final Consumer<String, DataTransferEvent> consumer = new KafkaConsumer<>(props);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(kafkaTopic));
        return consumer;
    }

    private boolean processCallbackMessage(DataTransferEvent dte) {
        logger.info("Processing DTE for task {}, workflow {} and status {}",
                dte.getTaskId(), dte.getWorkflowId(), dte.getTransferStatus());
        Optional<CallbackWorkflowEntity> workflowEntityOp = callbackWorkflowStore.getWorkflowEntity(dte.getWorkflowId(), dte.getTaskId(), 1);
        if (workflowEntityOp.isPresent()) {
            logger.info("Found a callback workflow to continue workflow {}", dte.getWorkflowId());
            CallbackWorkflowEntity callbackWorkflowEntity = workflowEntityOp.get();
            String[] startTasks = {callbackWorkflowEntity.getStartTaskId()};

            try {
                Map<String, AbstractTask> taskMap = callbackWorkflowEntity.getTaskMap();
                Map<String, Map<String, String>> taskValueMap = callbackWorkflowEntity.getTaskValueMap();

                // Initialize task data
                for (String key : taskMap.keySet()) {
                    TaskUtil.deserializeTaskData(taskMap.get(key), taskValueMap.get(key));
                }

                String workflowId = this.workflowOperator.buildAndRunWorkflow(taskMap, startTasks);
                logger.info("Successfully submitted callback workflow {} for incoming workflow {}", workflowId, dte.getWorkflowId());
            } catch (Exception e) {
                logger.error("Failed in executing callback workflow for worrkflow {}", dte.getWorkflowId());
            }
        } else {
            logger.warn("Didn't find a callback workflow for workflow {}", dte.getWorkflowId());
        }
        return true;
    }

    private void runKafkaConsumer() {

        final Consumer<String, DataTransferEvent> mftEventConsumer = createConsumer();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            while (true) {
                ConsumerRecords<String, DataTransferEvent> consumerRecords = mftEventConsumer.poll(Duration.ofMillis(1000));
                if (!consumerRecords.isEmpty()) {

                    CompletionService<Boolean> executorCompletionService = new ExecutorCompletionService<>(kafkaMessageProcessPool);

                    List<Future<Boolean>> processingFutures = new ArrayList<>();

                    for (TopicPartition partition : consumerRecords.partitions()) {
                        List<ConsumerRecord<String, DataTransferEvent>> partitionRecords = consumerRecords.records(partition);
                        for (ConsumerRecord<String, DataTransferEvent> record : partitionRecords) {
                            processingFutures.add(executorCompletionService.submit(() -> {
                                boolean success = processCallbackMessage(record.value());
                                logger.info("Processing DTE for task " + record.value().getTaskId() + " : " + success);
                                return success;
                            }));

                            mftEventConsumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(record.offset() + 1)));
                        }
                    }

                    for (Future<Boolean> f : processingFutures) {
                        try {
                            executorCompletionService.take().get();
                        } catch (Exception e) {
                            logger.error("Failed processing DTE", e);
                        }
                    }
                    logger.info("All messages processed. Moving to next round");
                }
            }
        });
    }

    public void init() throws Exception {
        workflowOperator = new WorkflowOperator();
        workflowOperator.init(clusterName, workflowManagerName, zkAddress, callbackWorkflowStore);
        runKafkaConsumer();
        logger.info("Successfully initialized DatasyncWorkflow Manager");
    }

    public void submitDataSyncWorkflow(WorkflowInvocationRequest request) throws Exception {
//        AsyncDataTransferTask dt1 = new AsyncDataTransferTask();
//        dt1.setSourceResourceId(workflowInvocationRequest.getMessage().getSourceResourceId());
//        dt1.setDestinationResourceId(workflowInvocationRequest.getMessage().getDestinationResourceId());
//        dt1.setSourceCredToken(workflowInvocationRequest.getMessage().getSourceCredentialToken());
//        dt1.setDestinationCredToken(workflowInvocationRequest.getMessage().getDestinationCredentialToken());
//        dt1.setTenantId(workflowInvocationRequest.getMessage().getTenantId());
//        dt1.setCallbackUrl(callbackURL);
//        dt1.setMftHost(mftHost);
//        dt1.setMftPort(mftPort);
//        dt1.setMftClientId(mftClientId);
//        dt1.setMftClientSecret(mftClientSecret);
//        dt1.setUserId(workflowInvocationRequest.getMessage().getUsername());
//        dt1.setCurrentSection(1);
//        dt1.setTaskId("dt-" + UUID.randomUUID().toString());
//        dt1.setMftCallbackStoreHost(datasyncWmHost);
//        dt1.setMftCallbackStorePort(datasyncWmPort);

        /*DataTransferPreValidationTask dt1 = new DataTransferPreValidationTask();
        dt1.setTenantId(request.getMessage().getTenantId());
        dt1.setCustosHost(custosHost);
        dt1.setCustosPort(custosPort);
        dt1.setSourceResourceId(request.getMessage().getSourceResourceId());
        dt1.setSourceCredToken(request.getMessage().getSourceCredentialToken());
        dt1.setDestinationResourceId(request.getMessage().getDestinationResourceId());
        dt1.setDestinationCredToken(request.getMessage().getDestinationCredentialToken());
        dt1.setDrmsHost(drmsHost);
        dt1.setDrmsPort(drmsPort);
        dt1.setCustosId(custosId);
        dt1.setCustosSecret(custosSecret);
        dt1.setAuthToken(request.getMessage().getAuthToken());
        dt1.setUserId(request.getMessage().getUsername());
        dt1.setTaskId("dt-" + UUID.randomUUID().toString());
        dt1.setCurrentSection(1);

        Map<String, AbstractTask> taskMap = new HashMap<>();
        taskMap.put(dt1.getTaskId(), dt1);
        //taskMap.put(bt2.getTaskId(), bt2);
        //taskMap.put(bt3.getTaskId(), bt3);
        //taskMap.put(bt4.getTaskId(), bt4);
        //String[] startTaskIds = {bt1.getTaskId(), bt2.getTaskId(), bt4.getTaskId()};
        String[] startTaskIds = {dt1.getTaskId()};
        String workflowId = workflowOperator.buildAndRunWorkflow(taskMap, startTaskIds);
        logger.info("Launched workflow {}", workflowId);*/
    }
}
