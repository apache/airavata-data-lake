package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User;

public class UserServiceImpl extends GenericService<User> implements UserService {

    public UserServiceImpl(Connector connector) {
        super(connector);
    }

    @Override
    Class<User> getEntityType() {
        return User.class;
    }
}
