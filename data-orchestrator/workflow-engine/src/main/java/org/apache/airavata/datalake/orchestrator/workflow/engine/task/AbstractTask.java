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
    private OutPort outPort;

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
            deserializeTaskData(this, getCallbackContext().getTaskConfig().getConfigMap());
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
        onCancel();
    }

    public abstract TaskResult onRun();

    public abstract void onCancel();

    public OutPort getOutPort() {
        return outPort;
    }

    public void setOutPort(OutPort outPort) {
        this.outPort = outPort;
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

    private <T extends AbstractTask> void deserializeTaskData(T instance, Map<String, String> params) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {

        List<Field> allFields = new ArrayList<>();
        Class genericClass = instance.getClass();

        while (AbstractTask.class.isAssignableFrom(genericClass)) {
            Field[] declaredFields = genericClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                allFields.add(declaredField);
            }
            genericClass = genericClass.getSuperclass();
        }

        for (Field classField : allFields) {
            TaskParam param = classField.getAnnotation(TaskParam.class);
            if (param != null) {
                if (params.containsKey(param.name())) {
                    classField.setAccessible(true);
                    PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(this, classField.getName());
                    Method writeMethod = PropertyUtils.getWriteMethod(propertyDescriptor);
                    Class<?>[] methodParamType = writeMethod.getParameterTypes();
                    Class<?> writeParameterType = methodParamType[0];

                    if (writeParameterType.isAssignableFrom(String.class)) {
                        writeMethod.invoke(instance, params.get(param.name()));
                    } else if (writeParameterType.isAssignableFrom(Integer.class) ||
                            writeParameterType.isAssignableFrom(Integer.TYPE)) {
                        writeMethod.invoke(instance, Integer.parseInt(params.get(param.name())));
                    } else if (writeParameterType.isAssignableFrom(Long.class) ||
                            writeParameterType.isAssignableFrom(Long.TYPE)) {
                        writeMethod.invoke(instance, Long.parseLong(params.get(param.name())));
                    } else if (writeParameterType.isAssignableFrom(Boolean.class) ||
                            writeParameterType.isAssignableFrom(Boolean.TYPE)) {
                        writeMethod.invoke(instance, Boolean.parseBoolean(params.get(param.name())));
                    } else if (TaskParamType.class.isAssignableFrom(writeParameterType)) {
                        Constructor<?> ctor = writeParameterType.getConstructor();
                        Object obj = ctor.newInstance();
                        ((TaskParamType)obj).deserialize(params.get(param.name()));
                        writeMethod.invoke(instance, obj);
                    }
                }
            }
        }

        for (Field classField : allFields) {
            TaskOutPort outPort = classField.getAnnotation(TaskOutPort.class);
            if (outPort != null) {
                classField.setAccessible(true);
                OutPort op = new OutPort();
                op.setNextTaskId(params.get(outPort.name()));
            }
        }
    }
}
