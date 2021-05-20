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

package org.apache.airavata.datalake.orchestrator.workflow.engine.services.wm;

import org.apache.airavata.datalake.orchestrator.workflow.engine.task.AbstractTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.OutPort;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl.ExampleBlockingTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication()
@ComponentScan("org.apache.airavata.datalake.orchestrator.workflow.engine.services.wm")
public class DataSyncWorkflowManager implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(DataSyncWorkflowManager.class);

    @org.springframework.beans.factory.annotation.Value("${cluster.name}")
    private String clusterName;

    @org.springframework.beans.factory.annotation.Value("${datasync.wm.name}")
    private String workflowManagerName;

    @org.springframework.beans.factory.annotation.Value("${zookeeper.connection}")
    private String zkAddress;

    @Override
    public void run(String... args) throws Exception {
        WorkflowOperator workflowOperator = new WorkflowOperator();
        workflowOperator.init(clusterName, workflowManagerName, zkAddress);

        ExampleBlockingTask bt1 = new ExampleBlockingTask();
        bt1.setTaskId("bt1-" + UUID.randomUUID());

        ExampleBlockingTask bt2 = new ExampleBlockingTask();
        bt2.setTaskId("bt2-" + UUID.randomUUID());

        // Setting dependency
        bt1.setOutPort(new OutPort().setNextTaskId(bt2.getTaskId()));

        Map<String, AbstractTask> taskMap = new HashMap<>();
        taskMap.put(bt1.getTaskId(), bt1);
        taskMap.put(bt2.getTaskId(), bt2);
        String workflowId = workflowOperator.buildAndRunWorkflow(taskMap, bt1.getTaskId());
        logger.info("Launched workflow {}", workflowId);
    }

    public static void main(String args[]) throws Exception {
        SpringApplication.run(DataSyncWorkflowManager.class);
    }
}
