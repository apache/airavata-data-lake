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
import org.apache.helix.task.Task;
import org.apache.helix.task.TaskCallbackContext;
import org.apache.helix.task.TaskResult;
import org.apache.helix.task.UserContentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractTask extends UserContentStore implements Task {

    private final static Logger logger = LoggerFactory.getLogger(AbstractTask.class);

    private TaskCallbackContext callbackContext;

    @TaskOutPort(name = "nextTask")
    private OutPort outPort;

    @TaskParam(name = "taskId")
    private String taskId;

    @TaskParam(name = "retryCount")
    private int retryCount = 3;

    public AbstractTask() {

    }

    @Override
    public TaskResult run() {
        try {
            String helixTaskId = this.callbackContext.getTaskConfig().getId();
            logger.info("Running task {}", helixTaskId);
            deserializeTaskData(this, this.callbackContext.getTaskConfig().getConfigMap());
        } catch (Exception e) {
            logger.error("Failed at deserializing task data", e);
            return new TaskResult(TaskResult.Status.FAILED, "Failed in deserializing task data");
        }
        return onRun();
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
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public TaskCallbackContext getCallbackContext() {
        return callbackContext;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setCallbackContext(TaskCallbackContext callbackContext) {
        this.callbackContext = callbackContext;
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
                    if (classField.getType().isAssignableFrom(String.class)) {
                        classField.set(instance, params.get(param.name()));
                    } else if (classField.getType().isAssignableFrom(Integer.class) ||
                            classField.getType().isAssignableFrom(Integer.TYPE)) {
                        classField.set(instance, Integer.parseInt(params.get(param.name())));
                    } else if (classField.getType().isAssignableFrom(Long.class) ||
                            classField.getType().isAssignableFrom(Long.TYPE)) {
                        classField.set(instance, Long.parseLong(params.get(param.name())));
                    } else if (classField.getType().isAssignableFrom(Boolean.class) ||
                            classField.getType().isAssignableFrom(Boolean.TYPE)) {
                        classField.set(instance, Boolean.parseBoolean(params.get(param.name())));
                    } else if (TaskParamType.class.isAssignableFrom(classField.getType())) {
                        Class<?> clazz = classField.getType();
                        Constructor<?> ctor = clazz.getConstructor();
                        Object obj = ctor.newInstance();
                        ((TaskParamType)obj).deserialize(params.get(param.name()));
                        classField.set(instance, obj);
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
