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

package org.apache.airavata.datalake.orchestrator.workflow.engine.services.wm.datasync;

public class MFTCallbackEntity {
    private String mftTransferId;
    private int prevSectionIndex;
    private String workflowId;
    private String taskId;

    public String getMftTransferId() {
        return mftTransferId;
    }

    public void setMftTransferId(String mftTransferId) {
        this.mftTransferId = mftTransferId;
    }

    public int getPrevSectionIndex() {
        return prevSectionIndex;
    }

    public void setPrevSectionIndex(int prevSectionIndex) {
        this.prevSectionIndex = prevSectionIndex;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
