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

package org.apache.airavata.datalake.orchestrator.workflow.engine.monitor.filter.mft;

import org.apache.kafka.common.serialization.Deserializer;

public class DataTransferEventDeserializer implements Deserializer<DataTransferEvent> {
    @Override
    public DataTransferEvent deserialize(String s, byte[] bytes) {
        String deserializedData = new String(bytes);
        String[] parts = deserializedData.split("/");
        DataTransferEvent dte = new DataTransferEvent();
        dte.setTaskId(parts[0]);
        dte.setWorkflowId(parts[1]);
        dte.setTransferStatus(parts[2]);
        return dte;
    }
}
