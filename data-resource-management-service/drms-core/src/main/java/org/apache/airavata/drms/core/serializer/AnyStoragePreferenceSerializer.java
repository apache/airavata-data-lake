package org.apache.airavata.drms.core.serializer;

import com.google.protobuf.Descriptors;
import org.apache.airavata.datalake.drms.storage.AnyStoragePreference;
import org.apache.airavata.datalake.drms.storage.preference.s3.S3StoragePreference;
import org.apache.airavata.datalake.drms.storage.preference.ssh.SSHStoragePreference;
import org.apache.airavata.drms.core.constants.StoragePreferenceConstants;

import java.util.HashMap;
import java.util.Map;

public class AnyStoragePreferenceSerializer {

    public static Map<String, Object> serializeToMap(AnyStoragePreference anyStorage) {

        Map<String, Object> fields = new HashMap<>();
        Map<Descriptors.FieldDescriptor, Object> allFields = null;
        switch (anyStorage.getStorageCase()) {
            case SSH_STORAGE_PREFERENCE:
                SSHStoragePreference sshStorage = anyStorage.getSshStoragePreference();
                allFields = sshStorage.getAllFields();
                fields.put(StoragePreferenceConstants.STORAGE_PREFERENCE_TYPE_LABEL, StoragePreferenceConstants.SSH_STORAGE_PREFERENCE_TYPE_LABEL);
                break;
            case S3_STORAGE_PREFERENCE:
                S3StoragePreference s3Storage = anyStorage.getS3StoragePreference();
                allFields = s3Storage.getAllFields();
                fields.put(StoragePreferenceConstants.STORAGE_PREFERENCE_TYPE_LABEL, StoragePreferenceConstants.S3_STORAGE_PREFERENCE_TYPE_LABEL);
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
