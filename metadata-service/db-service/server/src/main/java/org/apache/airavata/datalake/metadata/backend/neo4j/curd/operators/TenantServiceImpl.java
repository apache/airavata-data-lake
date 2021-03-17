package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant;

public class TenantServiceImpl extends GenericService<Tenant> implements TenantService {
    public TenantServiceImpl(Connector connector) {
        super(connector);
    }

    @Override
    Class<Tenant> getEntityType() {
        return Tenant.class;
    }
}
