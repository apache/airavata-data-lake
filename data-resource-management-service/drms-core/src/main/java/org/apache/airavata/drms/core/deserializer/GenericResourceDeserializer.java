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
import org.apache.airavata.datalake.drms.storage.AnyStorage;
import org.apache.airavata.datalake.drms.storage.AnyStoragePreference;
import org.apache.airavata.drms.core.constants.ResourceConstants;
import org.apache.airavata.drms.core.constants.StorageConstants;
import org.apache.airavata.drms.core.constants.StoragePreferenceConstants;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.types.Node;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenericResourceDeserializer {

    public static List<GenericResource> deserializeList(List<Record> neo4jRecords) throws Exception {
        List<GenericResource> resourceList = new ArrayList<>();
        for (Record record : neo4jRecords) {
            InternalRecord internalRecord = (InternalRecord) record;
            List<Value> values = internalRecord.values();
            if (values.size() == 3) {
                Value resourceValue = values.get(0);
                Value prfValue = values.get(1);
                Value stoValue = values.get(2);
                Node resourceNode = resourceValue.asNode();
                Node prefNode = prfValue.asNode();
                Node stoNode = stoValue.asNode();
                if (resourceNode.hasLabel(ResourceConstants.RESOURCE_LABEL) &&
                        prefNode.hasLabel(StoragePreferenceConstants.STORAGE_PREFERENCE_LABEL) &&
                        stoNode.hasLabel(StorageConstants.STORAGE_LABEL)) {

                    AnyStorage storage = AnyStorageDeserializer.deriveStorageFromMap(stoNode.asMap());
                    AnyStoragePreference preference = AnyStoragePreferenceDeserializer.deriveStoragePrefFromMap(
                            prefNode.asMap(), storage);
                    GenericResource genericResource = deriveGenericResourceFromMap(resourceNode.asMap(), preference);
                    resourceList.add(genericResource);
                }
            }
        }
        return resourceList;
    }

    public static GenericResource deriveGenericResourceFromMap(Map<String, Object> fixedMap,
                                                               AnyStoragePreference preference) throws Exception {

        GenericResource.Builder genericResourceBuilder = GenericResource.newBuilder();
        setObjectFieldsUsingMap(genericResourceBuilder, fixedMap);
        switch (preference.getStorageCase()){
            case S3STORAGEPREFERENCE:
                genericResourceBuilder.setS3Preference(preference.getS3StoragePreference());
                break;
            case SSHSTORAGEPREFERENCE:
                genericResourceBuilder.setSshPreference(preference.getSshStoragePreference());
                break;
        }

        return genericResourceBuilder.build();
    }

    private static void setObjectFieldsUsingMap(Object target, Map<String, Object> values) {
        for (String field :values.keySet()) {
            BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);
            beanWrapper.setPropertyValue(field, values.get(field));
        }
    }
}
