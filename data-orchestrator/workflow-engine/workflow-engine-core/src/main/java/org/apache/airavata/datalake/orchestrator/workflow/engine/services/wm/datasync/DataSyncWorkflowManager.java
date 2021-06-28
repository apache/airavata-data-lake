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

package org.apache.airavata.datalake.orchestrator.workflow.engine.services.wm.datasync;

import org.apache.airavata.datalake.orchestrator.workflow.engine.monitor.filter.mft.DataTransferEvent;
import org.apache.airavata.datalake.orchestrator.workflow.engine.monitor.filter.mft.DataTransferEventDeserializer;
import org.apache.airavata.datalake.orchestrator.workflow.engine.services.wm.CallbackWorkflowEntity;
import org.apache.airavata.datalake.orchestrator.workflow.engine.services.wm.CallbackWorkflowStore;
import org.apache.airavata.datalake.orchestrator.workflow.engine.services.wm.WorkflowOperator;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.AbstractTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.OutPort;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.TaskUtil;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl.AsyncDataTransferTask;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

    public void submitDataSyncWorkflow() throws Exception {
        AsyncDataTransferTask dt1 = new AsyncDataTransferTask();
        dt1.setSourceResourceId("");
        dt1.setDestinationResourceId("");
        dt1.setSourceCredToken("");
        dt1.setDestinationCredToken("");
        dt1.setCallbackUrl("localhost:33335");
        dt1.setMftHost("localhost");
        dt1.setMftPort(7004);
        dt1.setMftClientId("");
        dt1.setMftClientSecret("");
        dt1.setUserId("dimuthu");
        dt1.setCurrentSection(1);
        dt1.setTaskId("dt-" + UUID.randomUUID().toString());
        dt1.setMftCallbackStoreHost(datasyncWmHost);
        dt1.setMftCallbackStorePort(datasyncWmPort);

        Map<String, AbstractTask> taskMap = new HashMap<>();
        taskMap.put(dt1.getTaskId(), dt1);
        //taskMap.put(bt2.getTaskId(), bt2);
        //taskMap.put(bt3.getTaskId(), bt3);
        //taskMap.put(bt4.getTaskId(), bt4);
        //String[] startTaskIds = {bt1.getTaskId(), bt2.getTaskId(), bt4.getTaskId()};
        String[] startTaskIds = {dt1.getTaskId()};
        String workflowId = workflowOperator.buildAndRunWorkflow(taskMap, startTaskIds);
        logger.info("Launched workflow {}", workflowId);
    }
}
