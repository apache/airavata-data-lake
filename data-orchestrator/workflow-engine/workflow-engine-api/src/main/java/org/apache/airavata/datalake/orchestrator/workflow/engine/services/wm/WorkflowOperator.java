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
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.NonBlockingTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.OutPort;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.TaskParamType;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.BlockingTaskDef;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.NonBlockingTaskDef;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.TaskOutPort;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.TaskParam;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.helix.HelixManager;
import org.apache.helix.HelixManagerFactory;
import org.apache.helix.InstanceType;
import org.apache.helix.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class WorkflowOperator {

    private static final long WORKFLOW_EXPIRY_TIME = 1 * 1000;
    private static final long TASK_EXPIRY_TIME = 24 * 60 * 60 * 1000;
    private static final int PARALLEL_JOBS_PER_WORKFLOW = 20;

    private final static Logger logger = LoggerFactory.getLogger(WorkflowOperator.class);

    private TaskDriver taskDriver;
    private HelixManager helixManager;

    public void init(String clusterName, String workflowManagerName, String zkAddress) throws Exception {
        helixManager = HelixManagerFactory.getZKHelixManager(clusterName, workflowManagerName,
                InstanceType.SPECTATOR, zkAddress);
        helixManager.connect();

        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        if (helixManager != null && helixManager.isConnected()) {
                            helixManager.disconnect();
                        }
                    }
                }
        );

        taskDriver = new TaskDriver(helixManager);
    }

    public void destroy() {
        if (helixManager != null) {
            helixManager.disconnect();
        }
    }

    public String buildAndRunWorkflow(Map<String, AbstractTask> taskMap, String[] startTaskIds) throws Exception {

        if (taskDriver == null) {
            throw new Exception("Workflow operator needs to be initialized");
        }

        String workflowName = UUID.randomUUID().toString();
        Workflow.Builder workflowBuilder = new Workflow.Builder(workflowName).setExpiry(0);

        for (String startTaskId: startTaskIds) {
            buildWorkflowRecursively(workflowBuilder, startTaskId, taskMap);
        }

        WorkflowConfig.Builder config = new WorkflowConfig.Builder()
                .setFailureThreshold(0)
                .setAllowOverlapJobAssignment(true);

        workflowBuilder.setWorkflowConfig(config.build());
        workflowBuilder.setExpiry(WORKFLOW_EXPIRY_TIME);
        Workflow workflow = workflowBuilder.build();

        taskDriver.start(workflow);
        return workflowName;
    }

    private void continueNonBlockingRest(Map<String, AbstractTask> taskMap, String nonBlockingTaskId, int currentSection) {

    }

    private void buildWorkflowRecursively(Workflow.Builder workflowBuilder, String nextTaskId, Map<String, AbstractTask> taskMap)
            throws Exception{
        AbstractTask currentTask = taskMap.get(nextTaskId);

        if (currentTask == null) {
            logger.error("Couldn't find a task with id {} in the task map", nextTaskId);
            throw new Exception("Couldn't find a task with id " + nextTaskId +" in the task map");
        }

        BlockingTaskDef blockingTaskDef = currentTask.getClass().getAnnotation(BlockingTaskDef.class);
        NonBlockingTaskDef nonBlockingTaskDef = currentTask.getClass().getAnnotation(NonBlockingTaskDef.class);

        if (blockingTaskDef != null) {
            String taskName = blockingTaskDef.name();
            TaskConfig.Builder taskBuilder = new TaskConfig.Builder()
                    .setTaskId(currentTask.getTaskId())
                    .setCommand(taskName);

            Map<String, String> paramMap = serializeTaskData(currentTask);
            paramMap.forEach(taskBuilder::addConfig);

            List<TaskConfig> taskBuilds = new ArrayList<>();
            taskBuilds.add(taskBuilder.build());

            JobConfig.Builder job = new JobConfig.Builder()
                    .addTaskConfigs(taskBuilds)
                    .setFailureThreshold(0)
                    .setExpiry(WORKFLOW_EXPIRY_TIME)
                    .setTimeoutPerTask(TASK_EXPIRY_TIME)
                    .setNumConcurrentTasksPerInstance(20)
                    .setMaxAttemptsPerTask(currentTask.getRetryCount());

            workflowBuilder.addJob(currentTask.getTaskId(), job);

            List<OutPort> outPorts = getOutPortsOfTask(currentTask);

            for (OutPort outPort : outPorts) {
                if (outPort != null) {
                    workflowBuilder.addParentChildDependency(currentTask.getTaskId(), outPort.getNextTaskId());
                    logger.info("Parent to child dependency {} -> {}", currentTask.getTaskId(), outPort.getNextTaskId());
                    buildWorkflowRecursively(workflowBuilder, outPort.getNextTaskId(), taskMap);
                }
            }
        } else if (nonBlockingTaskDef != null) {

            NonBlockingTask nbTask = (NonBlockingTask) currentTask;

            String taskName = nonBlockingTaskDef.name();
            TaskConfig.Builder taskBuilder = new TaskConfig.Builder()
                    .setTaskId(currentTask.getTaskId())
                    .setCommand(taskName);

            Map<String, String> paramMap = serializeTaskData(currentTask);
            paramMap.forEach(taskBuilder::addConfig);

            List<TaskConfig> taskBuilds = new ArrayList<>();
            taskBuilds.add(taskBuilder.build());

            JobConfig.Builder job = new JobConfig.Builder()
                    .addTaskConfigs(taskBuilds)
                    .setFailureThreshold(0)
                    .setExpiry(WORKFLOW_EXPIRY_TIME)
                    .setTimeoutPerTask(TASK_EXPIRY_TIME)
                    .setNumConcurrentTasksPerInstance(20)
                    .setMaxAttemptsPerTask(currentTask.getRetryCount());

            workflowBuilder.addJob(currentTask.getTaskId(), job);

            continueNonBlockingRest(taskMap, nextTaskId, nbTask.getCurrentSection());
        } else {
            logger.error("Couldn't find the task def annotation in class {}", currentTask.getClass().getName());
            throw new Exception("Couldn't find the task def annotation in class " + currentTask.getClass().getName());
        }
    }

    public String getWorkflowStatus(String workflowName) {
        WorkflowContext workflowContext = taskDriver.getWorkflowContext(workflowName);
        TaskState workflowState = workflowContext.getWorkflowState();
        return workflowState.name();
    }

    public void stopWorkflow(String workflowName) {
        taskDriver.stop(workflowName);
    }

    public void resumeWorkflow(String workflowName) {
        taskDriver.resume(workflowName);
    }

    public void deleteWorkflow(String workflowName) {
        taskDriver.delete(workflowName);
    }

    private <T extends AbstractTask> Map<String, String> serializeTaskData(T data) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Map<String, String> result = new HashMap<>();
        for (Class<?> c = data.getClass(); c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field classField : fields) {
                TaskParam parm = classField.getAnnotation(TaskParam.class);
                try {
                    if (parm != null) {
                        Object propertyValue = PropertyUtils.getProperty(data, parm.name());
                        if (propertyValue instanceof TaskParamType) {
                            result.put(parm.name(), TaskParamType.class.cast(propertyValue).serialize());
                        } else {
                            result.put(parm.name(), propertyValue.toString());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to serialize task parameter {} in class {}", parm.name(), data.getClass().getName());
                    throw e;
                }

                TaskOutPort outPort = classField.getAnnotation(TaskOutPort.class);
                if (outPort != null) {
                    classField.setAccessible(true);
                    if (classField.get(data) != null) {
                        result.put(outPort.name(), ((OutPort) classField.get(data)).getNextTaskId().toString());
                    }
                }
            }
        }
        return result;
    }

    private <T extends AbstractTask> List<OutPort> getOutPortsOfTask(T taskObj) throws IllegalAccessException {

        List<OutPort> outPorts = new ArrayList<>();
        for (Class<?> c = taskObj.getClass(); c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                TaskOutPort outPortAnnotation = field.getAnnotation(TaskOutPort.class);
                if (outPortAnnotation != null) {
                    field.setAccessible(true);
                    OutPort outPort = (OutPort) field.get(taskObj);
                    outPorts.add(outPort);
                }
            }
        }
        return outPorts;
    }
}
