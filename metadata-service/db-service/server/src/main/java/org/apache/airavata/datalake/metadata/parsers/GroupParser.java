package org.apache.airavata.datalake.metadata.parsers;

import com.google.protobuf.GeneratedMessageV3;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Entity;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User;
import org.apache.airavata.datalake.metadata.mergers.Merger;
import org.apache.airavata.datalake.metadata.service.GroupMembership;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupParser.class);

    @Autowired
    private DozerBeanMapper dozerBeanMapper;


    @Override
    public Group parse(GeneratedMessageV3 entity, Entity parentEntity, ExecutionContext executionContext, Merger merger) {
        if (entity instanceof org.apache.airavata.datalake.metadata.service.Group) {
            org.apache.airavata.datalake.metadata.service.Group group = (org.apache.airavata.datalake.metadata.service.Group) entity;
            Group newParentGroup = null;
            if (parentEntity == null) {
                newParentGroup = dozerBeanMapper.map(group,
                        org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group.class);
                LOGGER.info("Creating group "+ newParentGroup.getName() + " class"+ newParentGroup.toString());
                executionContext.addNeo4JConvertedModels(newParentGroup.getSearchableId(),newParentGroup);
                newParentGroup.setExecutionContext(executionContext);
            } else if (parentEntity != null){
                newParentGroup = (Group) parentEntity;
                Group childGroup = dozerBeanMapper.map(group,
                        org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group.class);
                executionContext.addNeo4JConvertedModels(childGroup.getSearchableId(),childGroup);
                LOGGER.info("Creating group "+ newParentGroup.getName() + " class"+ newParentGroup.toString());
                newParentGroup.addChildGroup(childGroup,
                        childGroup.getCreatedAt() != 0 ? childGroup.getCreatedAt() : System.currentTimeMillis(),
                        childGroup.getLastModifiedAt() != 0 ? childGroup.getLastModifiedAt() : System.currentTimeMillis(),
                        null); // Improve this with relatioship propertie
                childGroup.setExecutionContext(executionContext);
                newParentGroup = childGroup;
            }

            List<GroupMembership> groupMemberships = group.getGroupMembershipList();

            if (!groupMemberships.isEmpty()) {
                Group finalNewParentGroup1 = newParentGroup;
                groupMemberships.forEach(mebership -> {
                    User usr = dozerBeanMapper.map(mebership.getUser(), org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User.class);
                    executionContext.addNeo4JConvertedModels(usr.getSearchableId(),usr);
                    finalNewParentGroup1.addChildUser(usr, mebership.getMembershipType(),
                            mebership.getCreatedAt() != 0 ? mebership.getCreatedAt() : System.currentTimeMillis(),
                            mebership.getLastModifiedAt() != 0 ? mebership.getLastModifiedAt() : System.currentTimeMillis(),
                            null);
                });

            }

            List<org.apache.airavata.datalake.metadata.service.Group> groups = group.getChildGroupsList();

            if (!groups.isEmpty()) {

                Group finalNewParentGroup = newParentGroup;
                groups.forEach(gr -> {
                    this.parse(gr, finalNewParentGroup, executionContext,merger);
                });
            }

            return newParentGroup;
        } else {
            String msg = "Wrong entity type detected for parser Group Parser, Expected Group";
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
    }

    @Override
    public Entity parse(GeneratedMessageV3 entity, ExecutionContext executionContext) {
        return this.parse(entity,null, executionContext,null);
    }

    @Override
    public Entity parse(GeneratedMessageV3 entity) {
        return this.parse(entity,null,new ExecutionContext(),null);
    }

    @Override
    public Entity parseAndMerge(GeneratedMessageV3 entity) {
        return null;
    }
}
