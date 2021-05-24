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

import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.NonBlockingSection;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.TaskParam;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class NonBlockingTask extends AbstractTask {

    private final static Logger logger = LoggerFactory.getLogger(NonBlockingTask.class);

    @TaskParam(name = "currentSection")
    private ThreadLocal<Integer> currentSection = new ThreadLocal<>();

    public NonBlockingTask() {
    }

    @Override
    public TaskResult onRun() {
        Class<?> c = this.getClass();
        Method[] allMethods = c.getMethods();
        for (Method method : allMethods) {
            NonBlockingSection nbs = method.getAnnotation(NonBlockingSection.class);
            if (nbs != null) {
                if (nbs.sectionIndex() == getCurrentSection()) {
                    try {
                        Object result = method.invoke(this);
                        return (TaskResult) result;
                    } catch (Exception e) {
                        logger.error("Failed to invoke designated section {}", getCurrentSection(), e);
                        return new TaskResult(TaskResult.Status.FAILED,
                                "Failed to invoke designated section " + getCurrentSection());
                    }
                }
            }
        }

        logger.error("Couldn't find a section matching section id {}", getCurrentSection());
        return new TaskResult(TaskResult.Status.FAILED, "Couldn't find a section matching section id " + getCurrentSection());
    }

    @Override
    public void onCancel() {

    }

    public Integer getCurrentSection() {
        return currentSection.get();
    }

    public void setCurrentSection(Integer currentSection) {
        this.currentSection.set(currentSection);
    }
}
