package org.apache.airavata.drms.api.persistance.mapper;

import com.google.protobuf.Descriptors;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.storage.AnyStorage;
import org.apache.airavata.datalake.drms.storage.s3.S3Storage;
import org.apache.airavata.datalake.drms.storage.ssh.SSHStorage;
import org.apache.airavata.drms.api.persistance.model.Resource;
import org.apache.airavata.drms.api.persistance.model.ResourceProperty;
import org.apache.airavata.drms.core.constants.StorageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StorageMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageMapper.class);

    public static AnyStorage map(Resource resource) throws Exception {

        AnyStorage.Builder anyStorageBuilder = AnyStorage.newBuilder();

        String type = null;
        for (ResourceProperty resourceProperty : resource.getResourceProperty()) {
            if (resourceProperty.getPropertyKey().equals("type")) {
                type = resourceProperty.getPropertyValue();
                break;
            }
        }

        switch (type) {
            case StorageConstants.SSH_STORAGE_TYPE_LABEL:
                SSHStorage.Builder builder = SSHStorage.newBuilder();
                setObjectFieldsUsingMap(builder, resource.getResourceProperty());
                SSHStorage sshStorage = builder.build();
                anyStorageBuilder.setSshStorage(sshStorage);
                break;
            case StorageConstants.S3_STORAGE_TYPE_LABEL:
                S3Storage.Builder s3Builder = S3Storage.newBuilder();
                setObjectFieldsUsingMap(s3Builder, resource.getResourceProperty());
                anyStorageBuilder.setS3Storage(s3Builder.build());
                break;
            default:
                throw new Exception("Unsupported storage type for deserializing : " + type);
        }

        return anyStorageBuilder.build();
    }

    public static Resource map(AnyStorage anyStorage, AuthenticatedUser authenticatedUser) {

        Map<String, Object> fields = new HashMap<>();
        Map<Descriptors.FieldDescriptor, Object> allFields = null;

        Set<ResourceProperty> resourcePropertySet = new HashSet<>();

        Resource prResource = new Resource();
        prResource.setResourceType(StorageConstants.STORAGE_LABEL);
        switch (anyStorage.getStorageCase()) {
            case SSH_STORAGE:
                SSHStorage sshStorage = anyStorage.getSshStorage();
                allFields = sshStorage.getAllFields();
                resourcePropertySet.add(new ResourceProperty(StorageConstants.STORAGE_TYPE_LABEL,
                        StorageConstants.SSH_STORAGE_TYPE_LABEL, prResource));

                prResource.setId(sshStorage.getStorageId());
                break;
            case S3_STORAGE:
                S3Storage s3Storage = anyStorage.getS3Storage();
                allFields = s3Storage.getAllFields();
                resourcePropertySet.add(new ResourceProperty(StorageConstants.STORAGE_TYPE_LABEL,
                        StorageConstants.S3_STORAGE_TYPE_LABEL, prResource));
                prResource.setId(s3Storage.getStorageId());
                break;
            case STORAGE_NOT_SET:
                break;
        }

        if (allFields != null) {
            allFields.forEach((descriptor, value) -> {
                String fieldName = descriptor.getJsonName();
                resourcePropertySet.add(new ResourceProperty(fieldName, value.toString(), prResource));
            });
        }

        resourcePropertySet.add(new ResourceProperty("tenantId", authenticatedUser.getTenantId(), prResource));
        prResource.setResourceProperty(resourcePropertySet);
        prResource.setTenantId(authenticatedUser.getTenantId());
        return prResource;
    }


    private static void setObjectFieldsUsingMap(Object target, Set<ResourceProperty> values) {
        for (ResourceProperty field : values) {
            BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);
            try {
                beanWrapper.setPropertyValue(field.getPropertyKey(), field.getPropertyValue());
            } catch (Exception ex) {
//                LOGGER.error(" Error occurred during field setting ", ex);
                continue;
            }
        }
    }
}
