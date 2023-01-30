package org.apache.airavata.drms.api.persistance.mapper;

import com.google.protobuf.Descriptors;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.drms.api.persistance.model.Resource;
import org.apache.airavata.drms.api.persistance.model.ResourceProperty;
import org.apache.custos.sharing.service.Entity;
import org.apache.custos.sharing.service.PermissionType;
import org.apache.custos.sharing.service.SharingMetadata;

import java.util.*;

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
        genericResourceBuilder.putProperties("createdTime", String.valueOf(entity.getCreatedAt()));
        genericResourceBuilder.putProperties("lastModifiedTime", String.valueOf(entity.getUpdatedAt()));
        Set<ResourceProperty> resourcePropertySet = resource.getResourceProperty();


        SharingMetadata sharingMetadata = entity.getSharingMetadata();
        if (sharingMetadata != null && !sharingMetadata.getPermissionsList().isEmpty()) {
            String permission = "";
            for (PermissionType permissionType : sharingMetadata.getPermissionsList()) {
                if (permission.length() == 0) {
                    permission = permissionType.getId();
                } else {
                    permission = permissionType.getId() + " " + permission;
                }

            }
            genericResourceBuilder.putProperties("permission", permission);
        }


        Iterator<ResourceProperty> iterator = resourcePropertySet.iterator();

        while (iterator.hasNext()) {
            ResourceProperty resourceProperty = iterator.next();
            if (resourceProperty.getPropertyKey().equals("resourcePath")) {
                genericResourceBuilder.setResourcePath(resourceProperty.getPropertyValue());
            }
            if (resourceProperty.getPropertyKey().equals("note")) {
                genericResourceBuilder.putProperties(resourceProperty.getPropertyKey(), resourceProperty.getPropertyValue());
            }

            if (resourceProperty.getPropertyKey().equals("image") || resourceProperty.getPropertyKey().equals("thumbnail")) {
                String[] urlArrays = resourceProperty.getPropertyValue().split("/");
                String imagePath = "https://gateway.iubemcenter.indiana.edu/resource-images/";
                String fullPath = imagePath + urlArrays[urlArrays.length - 1];
                genericResourceBuilder.putProperties(resourceProperty.getPropertyKey(), fullPath);
            }


//            genericResourceBuilder.putProperties(resourceProperty.getPropertyKey(), resourceProperty.getPropertyValue());

        }
        return genericResourceBuilder.build();
    }

    public static GenericResource map(Resource resource, Entity entity, List<String> permissions) {

        GenericResource genericResource = map(resource, entity);
        GenericResource.Builder builder =null;
        if (!permissions.isEmpty()) {
            String perm = "";
            for (String permission : permissions) {
                if (perm.isEmpty()) {
                    perm = permission;
                } else {

                    perm = perm + " " + permission;
                }

            }
             builder =  genericResource.toBuilder().putProperties("permission", perm);
        }
        return builder.build();

    }

    public static Resource map(GenericResource resource, Resource exResource, Entity entity,
                               AuthenticatedUser authenticatedUser) {

        Map<Descriptors.FieldDescriptor, Object> allFields = resource.getAllFields();

        Set<ResourceProperty> resourcePropertySet = new HashSet<>();

        Resource prResource = new Resource();

       Map<String,String> resourceMap =  resource.getPropertiesMap();

       for(Map.Entry<String,String> entry: resourceMap.entrySet()){
           resourcePropertySet.add(new ResourceProperty(entry.getKey(), entry.getValue(), prResource));
        }
       
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
        resourcePropertySet.add(new ResourceProperty("lastModifiedTime", String.valueOf(entity.getCreatedAt()), prResource));
        resourcePropertySet.add(new ResourceProperty("owner", entity.getOwnerId(), prResource));

        if(exResource != null){
          Set<ResourceProperty> properties =  exResource.getResourceProperty();
          for(ResourceProperty property: properties){
              if (property.getPropertyKey().equals("metadata")||
                      property.getPropertyKey().equals("firstName")||
                      property.getPropertyKey().equals("lastName")){
                  resourcePropertySet.add(property);
              }
          }
        }

        prResource.setId(entity.getId());
        prResource.setResourceProperty(resourcePropertySet);
        prResource.setTenantId(authenticatedUser.getTenantId());
        prResource.setResourceProperty(resourcePropertySet);
        prResource.setSourceTransferMapping(new HashSet<>());
        prResource.setDestinationTransferMapping(new HashSet<>());

        return prResource;
    }
}
