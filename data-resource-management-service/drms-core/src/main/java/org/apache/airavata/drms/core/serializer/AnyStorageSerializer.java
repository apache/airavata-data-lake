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
package org.apache.airavata.drms.core.serializer;

import com.google.protobuf.Descriptors;
import org.apache.airavata.datalake.drms.storage.AnyStorage;
import org.apache.airavata.datalake.drms.storage.s3.S3Storage;
import org.apache.airavata.datalake.drms.storage.ssh.SSHStorage;
import org.apache.airavata.drms.core.constants.StorageConstants;

import java.util.HashMap;
import java.util.Map;

public class AnyStorageSerializer {

    public static Map<String, Object> serializeToMap(AnyStorage anyStorage) {

        Map<String, Object> fields = new HashMap<>();
        Map<Descriptors.FieldDescriptor, Object> allFields = null;
        switch (anyStorage.getStorageCase()) {
            case SSH_STORAGE:
                SSHStorage sshStorage = anyStorage.getSshStorage();
                allFields = sshStorage.getAllFields();
                fields.put(StorageConstants.STORAGE_TYPE_LABEL, StorageConstants.SSH_STORAGE_TYPE_LABEL);
                break;
            case S3_STORAGE:
                S3Storage s3Storage = anyStorage.getS3Storage();
                allFields = s3Storage.getAllFields();
                fields.put(StorageConstants.STORAGE_TYPE_LABEL, StorageConstants.S3_STORAGE_TYPE_LABEL);
                break;
            case STORAGE_NOT_SET:
                break;
        }

        if (allFields != null) {
            allFields.forEach((descriptor, value) -> {
                String fieldName = descriptor.getJsonName();
                fields.put(fieldName, value);
            });
        }

        return fields;
    }
}
