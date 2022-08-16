package org.apache.airavata.drms.api.persistance.mapper;

import org.apache.airavata.datalake.drms.storage.AnyStorage;
import org.apache.airavata.datalake.drms.storage.AnyStoragePreference;
import org.apache.airavata.datalake.drms.storage.preference.s3.S3StoragePreference;
import org.apache.airavata.datalake.drms.storage.preference.ssh.SSHStoragePreference;
import org.apache.airavata.drms.api.persistance.model.Resource;
import org.apache.airavata.drms.api.persistance.model.ResourceProperty;
import org.apache.airavata.drms.core.constants.StoragePreferenceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public class StoragePreferenceMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoragePreferenceMapper.class);


    public static AnyStoragePreference map(Resource resource, AnyStorage anyStorage) throws Exception {

        String type = resource.getResourceType();
        AnyStoragePreference.Builder anyStoragePrefBuilder = AnyStoragePreference.newBuilder();

        switch (type) {
            case StoragePreferenceConstants.SSH_STORAGE_PREFERENCE_TYPE_LABEL:
                SSHStoragePreference.Builder builder = SSHStoragePreference.newBuilder();
                SSHStoragePreference sshStoragePreference = builder.build();
                anyStoragePrefBuilder.setSshStoragePreference(sshStoragePreference);
                setObjectFieldsUsingMap(anyStoragePrefBuilder,resource);
                break;
            case StoragePreferenceConstants.S3_STORAGE_PREFERENCE_TYPE_LABEL:
                S3StoragePreference.Builder s3Builder = S3StoragePreference.newBuilder();
                s3Builder.setStorage(anyStorage.getS3Storage());
                anyStoragePrefBuilder.setS3StoragePreference(s3Builder.build());
                setObjectFieldsUsingMap(anyStoragePrefBuilder,resource);
                break;
            default:
                throw new Exception("Unsupported storage type for deserializing : " + type);
        }


        return anyStoragePrefBuilder.build();
    }







    private static void setObjectFieldsUsingMap(Object target, Resource resource) {
        for (ResourceProperty field : resource.getResourceProperty()) {
            BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);
            try {
                beanWrapper.setPropertyValue(field.getKey(), field.getValue());
            } catch (Exception ex) {
                continue;
            }
        }
    }


}
