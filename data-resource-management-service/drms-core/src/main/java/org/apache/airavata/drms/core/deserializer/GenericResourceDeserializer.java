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

import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.commons.collections.map.HashedMap;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenericResourceDeserializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(GenericResourceDeserializer.class);

    public static List<GenericResource> deserializeList(List<Record> neo4jRecords) throws Exception {
        Map<Long, Node> nodeMap = new HashedMap();
        for (Record record : neo4jRecords) {
            InternalRecord internalRecord = (InternalRecord) record;
            List<Value> values = internalRecord.values();
            if (values.size() > 0) {
                Map<Long, Node> longNodeMap = values.stream().filter(val ->
                        val.toString().equals("NULL") ? false : true
                ).collect(Collectors.toMap(val -> val.asNode().id(),
                        Value::asNode, (existing, replacement) -> existing));
                nodeMap.putAll(longNodeMap);
            }
        }

        return deriveGenericResourceFromMap(nodeMap);
    }


    public static List<GenericResource> deserializeList(List<Record> neo4jRecords, List<String> keyValuePairs) throws Exception {
        Map<Long, Node> nodeMap = new HashedMap();
        Map<Long, Relationship> relationShip = new HashMap<>();
        for (Record record : neo4jRecords) {
            InternalRecord internalRecord = (InternalRecord) record;
            Map<String, Object> values = internalRecord.asMap();
            keyValuePairs.forEach(val -> {
                String[] keys = val.split(":");
                String resource = keys[0];
                String relationship = keys[1];

                InternalNode resNode = (InternalNode) values.get(resource);
                InternalRelationship relNode = (InternalRelationship) values.get(relationship);

                if (resNode != null) {
                    nodeMap.put(resNode.id(), resNode);
                    relationShip.put(resNode.id(), relNode);

                }
            });
        }

        return deriveGenericResourceFromMap(nodeMap,relationShip);
    }


    public static List<GenericResource> deriveGenericResourceFromMap(Map<Long, Node> nodeMap, Map<Long, Relationship> relationshipMap) throws Exception {
        return nodeMap.values().stream().map(node -> {
            GenericResource.Builder genericResourceBuilder = GenericResource.newBuilder();
            Iterator<String> iterator = node.labels().iterator();
            while (iterator.hasNext()) {
                genericResourceBuilder.setType(iterator.next());
            }
            for (String field : node.asMap().keySet()) {
                genericResourceBuilder.putProperties(field, String.valueOf(node.asMap().get(field)));
                if (field.equals("entityId")) {
                    genericResourceBuilder.setResourceId(String.valueOf(node.asMap().get(field)));
                }
                if (field.equals("resourceName") || field.equals("name")) {
                    genericResourceBuilder.setResourceName(String.valueOf(node.asMap().get(field)));
                }
                if (field.equals("resourcePath")) {
                    genericResourceBuilder.setResourcePath(String.valueOf(node.asMap().get(field)));
                }
                genericResourceBuilder.putProperties("permission",
                        relationshipMap.get(node.id()).asMap().get("permission").toString());
            }
            return genericResourceBuilder.build();
        }).collect(Collectors.toList());

    }


    public static List<GenericResource> deriveGenericResourceFromMap(Map<Long, Node> nodeMap) throws Exception {
        return nodeMap.values().stream().map(node -> {
            GenericResource.Builder genericResourceBuilder = GenericResource.newBuilder();
            Iterator<String> iterator = node.labels().iterator();
            while (iterator.hasNext()) {
                genericResourceBuilder.setType(iterator.next());
            }
            for (String field : node.asMap().keySet()) {
                genericResourceBuilder.putProperties(field, String.valueOf(node.asMap().get(field)));
                if (field.equals("entityId")) {
                    genericResourceBuilder.setResourceId(String.valueOf(node.asMap().get(field)));
                }
                if (field.equals("resourceName") || field.equals("name")) {
                    genericResourceBuilder.setResourceName(String.valueOf(node.asMap().get(field)));
                }
                if (field.equals("resourcePath")) {
                    genericResourceBuilder.setResourcePath(String.valueOf(node.asMap().get(field)));
                }
            }
            return genericResourceBuilder.build();
        }).collect(Collectors.toList());

    }

    private static void setObjectFieldsUsingMap(Object target, Map<String, Object> values) {
        for (String field : values.keySet()) {
            BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);
            beanWrapper.setPropertyValue(field, values.get(field));
        }
    }


}
