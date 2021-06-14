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

import org.apache.airavata.datalake.drms.storage.AnyStorage;
import org.apache.airavata.datalake.drms.storage.s3.S3Storage;
import org.apache.airavata.datalake.drms.storage.ssh.SSHStorage;
import org.apache.airavata.drms.core.constants.StorageConstants;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.types.Node;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnyStorageDeserializer {

    public static List<AnyStorage> deserializeList(List<Record> neo4jRecords) throws Exception {
        List<AnyStorage> storageList = new ArrayList<>();
        for (Record record : neo4jRecords) {
            InternalRecord internalRecord = (InternalRecord) record;
            List<Value> values = internalRecord.values();
            for (Value value : values) {
                if (!value.isNull()) {
                    Node node = value.asNode();
                    if (node.hasLabel(StorageConstants.STORAGE_LABEL)) {
                        storageList.add(deriveStorageFromMap(node.asMap()));
                    }
                }
            }
        }
        return storageList;
    }

    public static AnyStorage deriveStorageFromMap(Map<String, Object> fixedMap) throws Exception {

        Map<String, Object> asMap = new HashMap<>(fixedMap);
        AnyStorage.Builder anyStorageBuilder = AnyStorage.newBuilder();
        String type = (String) asMap.get(StorageConstants.STORAGE_TYPE_LABEL);
        asMap.remove(StorageConstants.STORAGE_TYPE_LABEL);

        switch (type) {
            case StorageConstants.SSH_STORAGE_TYPE_LABEL:
                SSHStorage.Builder builder = SSHStorage.newBuilder();
                setObjectFieldsUsingMap(builder, asMap);
                SSHStorage sshStorage = builder.build();
                anyStorageBuilder.setSshStorage(sshStorage);
                break;
            case StorageConstants.S3_STORAGE_TYPE_LABEL:
                S3Storage.Builder s3Builder = S3Storage.newBuilder();
                setObjectFieldsUsingMap(s3Builder, asMap);
                anyStorageBuilder.setS3Storage(s3Builder.build());
                break;
            default:
                throw new Exception("Unsupported storage type for deserializing : " + type);
        }

        return anyStorageBuilder.build();
    }

    private static void setObjectFieldsUsingMap(Object target, Map<String, Object> values) {
        for (String field : values.keySet()) {
            Class<?> someClass = target.getClass();

            try {
                Field someField = someClass.getField(field);
            } catch (Exception ex) {
                continue;
            }
            BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);
            beanWrapper.setPropertyValue(field, values.get(field));
        }
    }
}
