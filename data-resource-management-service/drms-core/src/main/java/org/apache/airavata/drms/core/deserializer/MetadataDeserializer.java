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

package org.apache.airavata.drms.core.deserializer;

import org.apache.airavata.datalake.drms.storage.Metadata;
import org.apache.airavata.datalake.drms.storage.MetadataNode;
import org.apache.airavata.drms.core.constants.MetadataConstants;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetadataDeserializer {

    public static List<MetadataNode> deserializeList(List<Record> neo4jRecords) throws Exception {
        List<MetadataNode> metadataNodeList = new ArrayList<>();
        for (Record record : neo4jRecords) {
            InternalRecord internalRecord = (InternalRecord) record;
            List<Value> values = internalRecord.values();

            if (values.size() == 1) {
                Value metadataValue = values.get(0);
                Node metadataNode = metadataValue.asNode();
                if (metadataNode.hasLabel(MetadataConstants.METADATA_LABEL)) {
                    MetadataNode.Builder metadataNodeBuilder = MetadataNode.newBuilder();
                    Map<String, Object> propertiesMap = metadataNode.asMap();
                    propertiesMap.forEach((key, val) ->
                    metadataNodeBuilder.addMetadata(Metadata.newBuilder().setKey(key).setValue(val.toString()).build()));
                    metadataNodeList.add(metadataNodeBuilder.build());
                }
            }
        }
        return metadataNodeList;
    }
}
