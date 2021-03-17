package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource;

public class ResourceServiceImpl extends GenericService<Resource> implements UserService {

    public ResourceServiceImpl(Connector connector) {
        super(connector);
    }

    @Override
    Class<Resource> getEntityType() {
        return Resource.class;
    }
}
