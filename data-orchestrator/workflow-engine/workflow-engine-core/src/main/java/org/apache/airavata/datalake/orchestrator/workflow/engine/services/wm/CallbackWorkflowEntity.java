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

import java.util.Map;

public class CallbackWorkflowEntity {
    private int prevSectionIndex;
    private Map<String, AbstractTask> taskMap;
    private String workflowId;
    private String startTaskId;

    public int getPrevSectionIndex() {
        return prevSectionIndex;
    }

    public void setPrevSectionIndex(int prevSectionIndex) {
        this.prevSectionIndex = prevSectionIndex;
    }

    public Map<String, AbstractTask> getTaskMap() {
        return taskMap;
    }

    public void setTaskMap(Map<String, AbstractTask> taskMap) {
        this.taskMap = taskMap;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getStartTaskId() {
        return startTaskId;
    }

    public void setStartTaskId(String startTaskId) {
        this.startTaskId = startTaskId;
    }
}
