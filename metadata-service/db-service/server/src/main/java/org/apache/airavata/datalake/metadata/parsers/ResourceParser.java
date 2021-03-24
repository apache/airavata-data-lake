package org.apache.airavata.datalake.metadata.parsers;

import com.google.protobuf.GeneratedMessageV3;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Entity;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User;
import org.apache.airavata.datalake.metadata.mergers.Merger;
import org.apache.airavata.datalake.metadata.service.ResourceSharings;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResourceParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceParser.class);

    @Autowired
    private DozerBeanMapper dozerBeanMapper;

    @Autowired
    private UserParser userParser;

    @Autowired
    private GroupParser groupParser;


    @Override
    public Resource parse(GeneratedMessageV3 entity, Entity parentEntity, ExecutionContext executionContext, Merger merger) {
        if (entity instanceof org.apache.airavata.datalake.metadata.service.Resource) {
            org.apache.airavata.datalake.metadata.service.Resource resource =
                    (org.apache.airavata.datalake.metadata.service.Resource) entity;

            Resource newParentResource = null;
            if (parentEntity == null) {
                newParentResource = dozerBeanMapper.map(resource,
                        org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource.class);
                executionContext.addNeo4JConvertedModels(newParentResource.getSearchableId(), newParentResource);
                newParentResource.setExecutionContext(executionContext);
            } else {
                newParentResource = (Resource) parentEntity;

                Resource childResource = dozerBeanMapper.map(resource,
                        org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource.class);
                executionContext.addNeo4JConvertedModels(newParentResource.getSearchableId(), newParentResource);

                newParentResource.addChildResource(childResource,
                        childResource.getCreatedAt() != 0 ? childResource.getCreatedAt() : System.currentTimeMillis(),
                        childResource.getLastModifiedAt() != 0 ? childResource.getLastModifiedAt() : System.currentTimeMillis(),
                        null); // Improve this with relationship properties
                childResource.setExecutionContext(executionContext);
                newParentResource = childResource;
            }


            List<ResourceSharings> resourceSharings = (resource).getSharingsList();

            if (!resourceSharings.isEmpty()) {
                Resource finalNewParentResource1 = newParentResource;
                resourceSharings.forEach(reshr -> {
                    if (!reshr.getUsersList().isEmpty()) {
                        reshr.getUsersList().forEach(shr -> {
                            User user = null;
                            Object obj = executionContext.getNeo4JConvertedModels(shr.getUsername() + "@" + shr.getTenantId());
                            if (obj != null) {
                                user = (User) obj;
                            } else {
                                user = (User) userParser.parse(shr, executionContext);
                                executionContext.addNeo4JConvertedModels(user.getSearchableId(), user);
                            }
                            finalNewParentResource1.shareWithAUser(user,
                                    reshr.getPermissionType(),
                                    reshr.getCreatedAt() != 0 ? reshr.getCreatedAt() : System.currentTimeMillis(),
                                    reshr.getLastModifiedAt() != 0 ? reshr.getLastModifiedAt() : System.currentTimeMillis(),
                                    null);

                        });

                    } else if (!reshr.getGroupsList().isEmpty()) {
                        reshr.getGroupsList().forEach(gr -> {
                            Group group = null;
                            Object obj = executionContext.getNeo4JConvertedModels(gr.getName() + "@" + gr.getTenantId());
                            if (obj != null) {
                                group = (Group) obj;
                            } else {
                                group = (Group) groupParser.parse(gr, executionContext);
                                executionContext.addNeo4JConvertedModels(group.getSearchableId(), group);
                            }

                            LOGGER.info("Group resource  " + group.toString());
                            finalNewParentResource1.shareWithAGroup(group, reshr.getPermissionType(),
                                    reshr.getCreatedAt() != 0 ? reshr.getCreatedAt() : System.currentTimeMillis(),
                                    reshr.getLastModifiedAt() != 0 ? reshr.getLastModifiedAt() : System.currentTimeMillis(),
                                    null);
                        });
                    }
                });
            }


            List<org.apache.airavata.datalake.metadata.service.Resource> resources = resource.getChildResourcesList();

            if (!resources.isEmpty()) {
                Resource finalNewParentResource = newParentResource;
                resources.forEach(gr -> {
                    this.parse(gr, finalNewParentResource, executionContext, merger);
                });
            }
            return newParentResource;
        } else {
            String msg = "Wrong entity type detected for parser Resource Parser, Expected Resource";
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
    }

    @Override
    public Entity parse(GeneratedMessageV3 entity, ExecutionContext executionContext) {
        return this.parse(entity, null, executionContext, null);
    }

    @Override
    public Entity parse(GeneratedMessageV3 entity) {
        return this.parse(entity, null, new ExecutionContext(),null);
    }

    @Override
    public Entity parseAndMerge(GeneratedMessageV3 entity) {
        return null;
    }
}
