package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group;

public class GroupServiceImpl extends GenericService<Group> implements GroupService {

    public GroupServiceImpl(Connector connector) {
        super(connector);
    }

    @Override
    Class<Group> getEntityType() {
        return Group.class;
    }
}
