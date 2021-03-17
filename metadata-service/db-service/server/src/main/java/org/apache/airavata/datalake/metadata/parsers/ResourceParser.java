package org.apache.airavata.datalake.metadata.parsers;

import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource;
import org.apache.airavata.datalake.metadata.service.ResourceSharings;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ResourceParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceParser.class);

    @Autowired
    private DozerBeanMapper dozerBeanMapper;


    public Resource parseResource(org.apache.airavata.datalake.metadata.service.Resource resource,
                                  Optional<Resource> parentResource) {
        Resource newParentResource;
        if (parentResource.isEmpty()) {
            newParentResource = dozerBeanMapper.map(resource,
                    org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource.class);
        } else {
            newParentResource = parentResource.get();
            Resource childResource = dozerBeanMapper.map(resource,
                    org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource.class);
            newParentResource.addChildResource(childResource,
                    childResource.getCreatedAt() != 0 ? childResource.getCreatedAt() : System.currentTimeMillis(),
                    childResource.getLastModifiedAt() != 0 ? childResource.getLastModifiedAt() : System.currentTimeMillis(),
                    null); // Improve this with relatioship properties
            newParentResource = childResource;
        }


         List<ResourceSharings> resourceSharings = resource.getSharingsList();

        if (! resourceSharings.isEmpty()) {
            resourceSharings.forEach(reshr-> {
                if (!reshr.getUsersList().isEmpty()) {
                    reshr.getUsersList().forEach(shr-> {
                       // newParentResource.shareWithAUser(shr,reshr.get);

                    });
                    
                } else if (!reshr.getGroupsList().isEmpty()) {

                }


            });


        }





        List<org.apache.airavata.datalake.metadata.service.Resource> resources = resource.getChildResourcesList();

        if (!resources.isEmpty()) {
            Resource finalNewParentResource = newParentResource;
            resources.forEach(gr -> {
                this.parseResource(gr, Optional.of(finalNewParentResource));
            });
        }
        return newParentResource;

    }


}
