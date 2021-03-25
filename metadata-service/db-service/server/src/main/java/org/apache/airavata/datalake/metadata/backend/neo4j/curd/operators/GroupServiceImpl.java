package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Entity;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group;
import org.neo4j.ogm.cypher.ComparisonOperator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupServiceImpl extends GenericService<Group> implements GroupService {

    public GroupServiceImpl(Connector connector) {
        super(connector);
    }

    @Override
    Class<Group> getEntityType() {
        return Group.class;
    }

    @Override
    public List<Group> find( Group group) {
        List<SearchOperator> searchOperatorList = new ArrayList<>();
        if (group.getTenantId() != null) {
            SearchOperator searchOperator = new SearchOperator();
            searchOperator.setKey("tenant_id");
            searchOperator.setValue(group.getTenantId());
            searchOperator.setComparisonOperator(ComparisonOperator.EQUALS);
            searchOperatorList.add(searchOperator);
        }

        if (group.getName() != null) {
            SearchOperator searchOperator = new SearchOperator();
            searchOperator.setKey("name");
            searchOperator.setValue(group.getName());
            searchOperator.setComparisonOperator(ComparisonOperator.EQUALS);
            searchOperatorList.add(searchOperator);
        }
        Collection<Group> groups = super.search(searchOperatorList);
        return new ArrayList<>(groups);
    }
}
