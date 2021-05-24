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
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl.ExampleNonBlockingTask;
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

        ExampleBlockingTask bt3 = new ExampleBlockingTask();
        bt3.setTaskId("bt3-" + UUID.randomUUID());

        ExampleBlockingTask bt4 = new ExampleBlockingTask();
        bt4.setTaskId("bt4-" + UUID.randomUUID());

        ExampleNonBlockingTask nbt1 = new ExampleNonBlockingTask();
        nbt1.setTaskId("nbt1-" + UUID.randomUUID());
        nbt1.setCurrentSection(2);

        // Setting dependency
        bt1.setOutPort(new OutPort().setNextTaskId(nbt1.getTaskId()));
        //bt2.setOutPort(new OutPort().setNextTaskId(bt3.getTaskId()));
        //bt4.setOutPort(new OutPort().setNextTaskId(bt3.getTaskId()));

        Map<String, AbstractTask> taskMap = new HashMap<>();
        taskMap.put(bt1.getTaskId(), bt1);
        taskMap.put(nbt1.getTaskId(), nbt1);
        //taskMap.put(bt2.getTaskId(), bt2);
        //taskMap.put(bt3.getTaskId(), bt3);
        //taskMap.put(bt4.getTaskId(), bt4);
        //String[] startTaskIds = {bt1.getTaskId(), bt2.getTaskId(), bt4.getTaskId()};
        String[] startTaskIds = {bt1.getTaskId()};
        String workflowId = workflowOperator.buildAndRunWorkflow(taskMap, startTaskIds);
        logger.info("Launched workflow {}", workflowId);
    }

    public static void main(String args[]) throws Exception {
        SpringApplication.run(DataSyncWorkflowManager.class);
    }
}
