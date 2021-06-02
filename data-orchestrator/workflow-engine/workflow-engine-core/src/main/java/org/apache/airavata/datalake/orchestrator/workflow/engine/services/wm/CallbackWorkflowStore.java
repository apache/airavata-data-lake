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

import java.util.*;

public class CallbackWorkflowStore {

    private final List<CallbackWorkflowEntity> workflowEntities = new ArrayList<>();

    public void saveWorkflowEntity(CallbackWorkflowEntity cwe) {
        workflowEntities.add(cwe);
    }

    public Optional<CallbackWorkflowEntity> getWorkflowEntity(String workflowId, String taskId, int prevSectionIndex) {
        return workflowEntities.stream().filter(e ->
                e.getWorkflowId().equals(workflowId) &&
                        e.getStartTaskId().equals(taskId) &&
                        e.getPrevSectionIndex() == prevSectionIndex).findFirst();
    }
}
