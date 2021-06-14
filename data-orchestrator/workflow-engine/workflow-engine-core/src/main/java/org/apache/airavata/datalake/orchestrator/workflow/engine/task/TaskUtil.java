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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskUtil {

    private final static Logger logger = LoggerFactory.getLogger(TaskUtil.class);

    public static <T extends AbstractTask> void deserializeTaskData(T instance, Map<String, String> params) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {

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
                    PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(instance, classField.getName());
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

    public static <T extends AbstractTask> Map<String, String> serializeTaskData(T data) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Map<String, String> result = new HashMap<>();
        for (Class<?> c = data.getClass(); c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field classField : fields) {
                TaskParam parm = classField.getAnnotation(TaskParam.class);
                try {
                    if (parm != null) {
                        Object propertyValue = PropertyUtils.getProperty(data, classField.getName());
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
}
