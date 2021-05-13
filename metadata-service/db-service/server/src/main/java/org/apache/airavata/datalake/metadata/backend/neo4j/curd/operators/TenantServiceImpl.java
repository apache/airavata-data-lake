package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant;
import org.neo4j.ogm.cypher.ComparisonOperator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TenantServiceImpl extends GenericService<Tenant> implements TenantService {
    public TenantServiceImpl(Connector connector) {
        super(connector);
    }

    @Override
    Class<Tenant> getEntityType() {
        return Tenant.class;
    }

    @Override
    public List<Tenant> find(Tenant tenant) {
        List<SearchOperator> searchOperatorList = new ArrayList<>();
        if (tenant.getTenantId() != null) {
            SearchOperator searchOperator = new SearchOperator();
            searchOperator.setKey("tenant_id");
            searchOperator.setValue(tenant.getTenantId());
            searchOperator.setComparisonOperator(ComparisonOperator.EQUALS);
            searchOperatorList.add(searchOperator);
        }

        if (tenant.getName() != null) {
            SearchOperator searchOperator = new SearchOperator();
            searchOperator.setKey("name");
            searchOperator.setValue(tenant.getName());
            searchOperator.setComparisonOperator(ComparisonOperator.EQUALS);
            searchOperatorList.add(searchOperator);
        }
        Collection<Tenant> tenants = super.search(searchOperatorList);
        return new ArrayList<>(tenants);
    }


    @Override
    public void createOrUpdate(Tenant tenant) {
        super.createOrUpdate(tenant);
    }
}
