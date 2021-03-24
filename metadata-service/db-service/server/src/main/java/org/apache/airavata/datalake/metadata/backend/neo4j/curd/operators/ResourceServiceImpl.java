package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Entity;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource;
import org.neo4j.ogm.cypher.ComparisonOperator;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResourceServiceImpl extends GenericService<Resource> implements ResourceService {

    public ResourceServiceImpl(Connector connector) {
        super(connector);
    }

    @Override
    Class<Resource> getEntityType() {
        return Resource.class;
    }


    @Override
    public boolean hasAccess(String username, String resourceName, String permissionType, String tenantId) {
        String query =
                "match (u:User{name:$username})-[:MEMBER_OF|HAS_CHILD_GROUP*]->(g:Group{tenant_id:$tenantId})" +
                        "-[r:HAS_ACCESS]->(m:Resource{tenant_id:$tenantId})-[l:HAS_CHILD_RESOURCE*]->  " +
                        "(p:Resource{tenant_id:$tenantId}) " +
                        "where r.permission_type=$permissionType   and m.name=$resourceName or  p.name=$resourceName return m,p";
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("username", username);
        parameterMap.put("permissionType", permissionType);
        parameterMap.put("resourceName", resourceName);
        parameterMap.put("tenantId", tenantId);
        Iterable<Map<String, Object>> mapIterable = super.execute(query, parameterMap);
        AtomicBoolean accessible = new AtomicBoolean(false);
        mapIterable.forEach(map -> {
            if (!map.isEmpty()) {
                accessible.set(true);
            }
        });
        return accessible.get();
    }

    @Override
    public List<Resource> find( Resource resource) {
        List<SearchOperator> searchOperatorList = new ArrayList<>();
        if (resource.getTenantId() != null) {
            SearchOperator searchOperator = new SearchOperator();
            searchOperator.setKey("tenant_id");
            searchOperator.setValue(resource.getTenantId());
            searchOperator.setComparisonOperator(ComparisonOperator.EQUALS);
            searchOperatorList.add(searchOperator);
        }

        if (resource.getName() != null) {
            SearchOperator searchOperator = new SearchOperator();
            searchOperator.setKey("name");
            searchOperator.setValue(resource.getName());
            searchOperator.setComparisonOperator(ComparisonOperator.EQUALS);
            searchOperatorList.add(searchOperator);
        }
        Collection<Resource> resources = super.search(searchOperatorList);
        return new ArrayList<>(resources);
    }
}
