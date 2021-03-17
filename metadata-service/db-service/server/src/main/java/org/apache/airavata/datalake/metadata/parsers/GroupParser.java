package org.apache.airavata.datalake.metadata.parsers;

import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User;
import org.apache.airavata.datalake.metadata.service.GroupMembership;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GroupParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupParser.class);

    @Autowired
    private DozerBeanMapper dozerBeanMapper;


    public Group parseGroup(org.apache.airavata.datalake.metadata.service.Group group, Optional<Group> parentGroup) {
        Group newParentGroup;
        if (parentGroup.isEmpty()) {
            newParentGroup = dozerBeanMapper.map(group,
                    org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group.class);
        } else {
            newParentGroup = parentGroup.get();
            Group childGroup = dozerBeanMapper.map(group,
                    org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group.class);
            newParentGroup.addChildGroup(childGroup,
                    childGroup.getCreatedAt() != 0 ? childGroup.getCreatedAt() : System.currentTimeMillis(),
                    childGroup.getLastModifiedAt() != 0 ? childGroup.getLastModifiedAt() : System.currentTimeMillis(),
                    null); // Improve this with relatioship propertie

            newParentGroup = childGroup;
        }

        List<GroupMembership> groupMemberships = group.getGroupMembershipList();

        if (!groupMemberships.isEmpty()) {
            Group finalNewParentGroup1 = newParentGroup;
            groupMemberships.forEach(mebership -> {
                User usr = dozerBeanMapper.map(mebership.getUser(), org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User.class);
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
                this.parseGroup(gr, Optional.of(finalNewParentGroup));
            });
        }
        return newParentGroup;

    }


}
