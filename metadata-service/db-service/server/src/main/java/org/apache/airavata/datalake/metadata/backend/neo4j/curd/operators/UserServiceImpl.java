package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User;
import org.neo4j.ogm.cypher.ComparisonOperator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserServiceImpl extends GenericService<User> implements UserService {

    public UserServiceImpl(Connector connector) {
        super(connector);
    }

    @Override
    Class<User> getEntityType() {
        return User.class;
    }


    @Override
    public List<User> find(User user) {
        List<SearchOperator> searchOperatorList = new ArrayList<>();
        if (user.getTenantId() != null) {
            SearchOperator searchOperator = new SearchOperator();
            searchOperator.setKey("tenant_id");
            searchOperator.setValue(user.getTenantId());
            searchOperator.setComparisonOperator(ComparisonOperator.EQUALS);
            searchOperatorList.add(searchOperator);
        }

        if (user.getUsername() != null) {
            SearchOperator searchOperator = new SearchOperator();
            searchOperator.setKey("username");
            searchOperator.setValue(user.getUsername());
            searchOperator.setComparisonOperator(ComparisonOperator.EQUALS);
            searchOperatorList.add(searchOperator);
        }
        Collection<User> users = super.search(searchOperatorList);
        return new ArrayList<>(users);
    }
}
