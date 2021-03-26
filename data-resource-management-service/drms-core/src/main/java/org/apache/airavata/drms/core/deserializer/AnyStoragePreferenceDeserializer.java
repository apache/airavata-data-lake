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
import org.apache.airavata.datalake.drms.storage.AnyStoragePreference;
import org.apache.airavata.datalake.drms.storage.preference.s3.S3StoragePreference;
import org.apache.airavata.datalake.drms.storage.preference.ssh.SSHStoragePreference;
import org.apache.airavata.drms.core.constants.StorageConstants;
import org.apache.airavata.drms.core.constants.StoragePreferenceConstants;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.types.Node;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnyStoragePreferenceDeserializer {
    public static List<AnyStoragePreference> deserializeList(List<Record> neo4jRecords) throws Exception {
        List<AnyStoragePreference> storagePrefList = new ArrayList<>();
        for (Record record : neo4jRecords) {
            InternalRecord internalRecord = (InternalRecord) record;
            List<Value> values = internalRecord.values();
            if (values.size() == 2) {
                Value prfValue = values.get(0);
                Value stoValue = values.get(1);
                Node prefNode = prfValue.asNode();
                Node stoNode = stoValue.asNode();
                if (prefNode.hasLabel(StoragePreferenceConstants.STORAGE_PREFERENCE_LABEL) && stoNode.hasLabel(StorageConstants.STORAGE_LABEL)) {
                    AnyStorage storage = AnyStorageDeserializer.deriveStorageFromMap(stoNode.asMap());
                    AnyStoragePreference preference = deriveStoragePrefFromMap(prefNode.asMap(), storage);
                    storagePrefList.add(preference);
                }
            }
        }
        return storagePrefList;
    }

    public static AnyStoragePreference deriveStoragePrefFromMap(Map<String, Object> fixedMap, AnyStorage anyStorage) throws Exception {

        Map<String, Object> asMap = new HashMap<>(fixedMap);
        AnyStoragePreference.Builder anyStoragePrefBuilder = AnyStoragePreference.newBuilder();
        String type = (String)asMap.get(StoragePreferenceConstants.STORAGE_PREFERENCE_TYPE_LABEL);
        asMap.remove(StoragePreferenceConstants.STORAGE_PREFERENCE_TYPE_LABEL);

        switch (type) {
            case StoragePreferenceConstants.SSH_STORAGE_PREFERENCE_TYPE_LABEL:
                SSHStoragePreference.Builder builder = SSHStoragePreference.newBuilder();
                setObjectFieldsUsingMap(builder, asMap);
                builder.setStorage(anyStorage.getSshStorage());
                SSHStoragePreference sshStoragePreference = builder.build();
                anyStoragePrefBuilder.setSshStoragePreference(sshStoragePreference);
                break;
            case StoragePreferenceConstants.S3_STORAGE_PREFERENCE_TYPE_LABEL:
                S3StoragePreference.Builder s3Builder = S3StoragePreference.newBuilder();
                setObjectFieldsUsingMap(s3Builder, asMap);
                s3Builder.setStorage(anyStorage.getS3Storage());
                anyStoragePrefBuilder.setS3StoragePreference(s3Builder.build());
                break;
            default:
                throw new Exception("Unsupported storage type for deserializing : " + type);
        }

        return anyStoragePrefBuilder.build();
    }

    private static void setObjectFieldsUsingMap(Object target, Map<String, Object> values) {
        for (String field :values.keySet()) {
            BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);
            beanWrapper.setPropertyValue(field, values.get(field));
        }
    }
}
