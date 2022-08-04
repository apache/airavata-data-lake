package org.apache.airavata.drms.api.persistance.mapper;

import com.google.protobuf.Descriptors;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.drms.api.persistance.model.Resource;
import org.apache.airavata.drms.api.persistance.model.ResourceProperty;
import org.apache.custos.sharing.service.Entity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ResourceMapper {

    public static GenericResource map(Resource resource, Entity entity) {

        GenericResource.Builder genericResourceBuilder = GenericResource.newBuilder();

        genericResourceBuilder.setType(entity.getType());
        genericResourceBuilder.setResourceId(entity.getId());
        genericResourceBuilder.setResourceName(entity.getName());
        if (!entity.getParentId().isEmpty()) {
            genericResourceBuilder.setParentId(entity.getParentId());
        }
        genericResourceBuilder.putProperties("owner", entity.getOwnerId());
        genericResourceBuilder.putProperties("description", entity.getDescription());
        genericResourceBuilder.putProperties("created_at", String.valueOf(entity.getCreatedAt()));
        genericResourceBuilder.putProperties("updated_at", String.valueOf(entity.getUpdatedAt()));
        Set<ResourceProperty> resourcePropertySet = resource.getResourceProperty();

        Iterator<ResourceProperty> iterator = resourcePropertySet.iterator();

        while (iterator.hasNext()) {
            ResourceProperty resourceProperty = iterator.next();
            if (resourceProperty.getKey().equals("resourcePath")) {
                genericResourceBuilder.setResourcePath(resourceProperty.getValue());
            }
            genericResourceBuilder.putProperties(resourceProperty.getKey(), resourceProperty.getValue());

        }
        return genericResourceBuilder.build();
    }

    public static Resource map(GenericResource resource, Entity entity, AuthenticatedUser authenticatedUser) {

        Map<Descriptors.FieldDescriptor, Object> allFields = resource.getAllFields();

        Set<ResourceProperty> resourcePropertySet = new HashSet<>();

        Resource prResource = new Resource();


        if (allFields != null) {
            allFields.forEach((descriptor, value) -> {
                String fieldName = descriptor.getJsonName();
                ResourceProperty resourceProperty = new ResourceProperty(fieldName, value.toString(), prResource);
                resourcePropertySet.add(resourceProperty);
            });
        }

        resourcePropertySet.add(new ResourceProperty("description", entity.getDescription(), prResource));
        resourcePropertySet.add(new ResourceProperty("resourceName", entity.getName(), prResource));
        resourcePropertySet.add(new ResourceProperty("createdTime", String.valueOf(entity.getCreatedAt()), prResource));
        resourcePropertySet.add(new ResourceProperty("tenantId", authenticatedUser.getTenantId(), prResource));
        resourcePropertySet.add(new ResourceProperty("entityId", entity.getId(), prResource));
        resourcePropertySet.add(new ResourceProperty("entityType", entity.getType(), prResource));
        resourcePropertySet.add(new ResourceProperty("lastModifiedTime", String.valueOf(entity.getCreatedAt()), prResource));
        resourcePropertySet.add(new ResourceProperty("owner", entity.getOwnerId(), prResource));
        resourcePropertySet.add(new ResourceProperty("firstName", authenticatedUser.getFirstName(), prResource));
        resourcePropertySet.add(new ResourceProperty("lastName", authenticatedUser.getFirstName(), prResource));


        prResource.setId(entity.getId());
        prResource.setResourceProperty(resourcePropertySet);
        prResource.setTenantId(authenticatedUser.getTenantId());
        prResource.setResourceProperty(resourcePropertySet);

        return prResource;
    }
}
