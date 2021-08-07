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

package org.apache.airavata.datalake.orchestrator.workflow.engine.task;

import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.TaskOutPort;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.TaskParam;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.helix.task.Task;
import org.apache.helix.task.TaskCallbackContext;
import org.apache.helix.task.TaskResult;
import org.apache.helix.task.UserContentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractTask extends UserContentStore implements Task {

    private final static Logger logger = LoggerFactory.getLogger(AbstractTask.class);

    private ThreadLocal<TaskCallbackContext> callbackContext = new ThreadLocal<>();
    private BlockingQueue<TaskCallbackContext> callbackContextQueue = new LinkedBlockingQueue<>();

    @TaskOutPort(name = "nextTask")
    private List<OutPort> outPorts = new ArrayList<>();

    @TaskParam(name = "taskId")
    private ThreadLocal<String> taskId = new ThreadLocal<>();

    @TaskParam(name = "retryCount")
    private ThreadLocal<Integer> retryCount = ThreadLocal.withInitial(()-> 3);

    public AbstractTask() {

    }

    @Override
    public TaskResult run() {
        try {
            TaskCallbackContext cbc = callbackContextQueue.poll();

            if (cbc == null) {
                logger.error("No callback context available");
                throw new Exception("No callback context available");
            }

            this.callbackContext.set(cbc);
            String helixTaskId = getCallbackContext().getTaskConfig().getId();
            logger.info("Running task {}", helixTaskId);
            TaskUtil.deserializeTaskData(this, getCallbackContext().getTaskConfig().getConfigMap());
        } catch (Exception e) {
            logger.error("Failed at deserializing task data", e);
            return new TaskResult(TaskResult.Status.FAILED, "Failed in deserializing task data");
        }

        try {
            return onRun();
        } catch (Exception e) {
            logger.error("Unknown error while running task {}", getTaskId(), e);
            return new TaskResult(TaskResult.Status.FAILED, "Failed due to unknown error");
        }
    }

    @Override
    public void cancel() {
        try {
            onCancel();
        } catch (Exception e) {
            logger.error("Unknown error while cancelling task {}", getTaskId(), e);
        }
    }

    public abstract TaskResult onRun() throws Exception;

    public abstract void onCancel() throws Exception;

    public List<OutPort> getOutPorts() {
        return outPorts;
    }

    public void addOutPort(OutPort outPort) {
        this.outPorts.add(outPort);
    }

    public int getRetryCount() {
        return retryCount.get();
    }

    public void setRetryCount(int retryCount) {
        this.retryCount.set(retryCount);
    }

    public TaskCallbackContext getCallbackContext() {
        return callbackContext.get();
    }

    public String getTaskId() {
        return taskId.get();
    }

    public void setTaskId(String taskId) {
        this.taskId.set(taskId);
    }

    public void setCallbackContext(TaskCallbackContext callbackContext) {
        logger.info("Setting callback context {}", callbackContext.getJobConfig().getId());
        try {
            this.callbackContextQueue.put(callbackContext);
        } catch (InterruptedException e) {
            logger.error("Failed to put callback context to the queue", e);
        }
    }
}
