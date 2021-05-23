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

package org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl;

import org.apache.airavata.datalake.orchestrator.workflow.engine.task.BlockingTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.BlockingTaskDef;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@BlockingTaskDef(name = "ExampleBlockingTask")
public class ExampleBlockingTask extends BlockingTask {

    private final static Logger logger = LoggerFactory.getLogger(ExampleBlockingTask.class);

    @Override
    public TaskResult runBlockingCode() {
        logger.info("Starting task {}", getTaskId());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (getTaskId().startsWith("bt1")) {
            try {
                logger.info("Task {} is sleeping", getTaskId());
                Thread.sleep(10000);
                //return new TaskResult(TaskResult.Status.FAILED, "Fail");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("Ending task {}", getTaskId());
        return new TaskResult(TaskResult.Status.COMPLETED, "Success");
    }
}
