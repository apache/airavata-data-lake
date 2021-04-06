package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public List<Resource> findResources(String username, String resourceName, String permissionType, String tenantId) {
        String query =
                "match (u:User{name:$username})-[:MEMBER_OF|HAS_CHILD_GROUP*]->(g:Group{tenant_id:$tenantId})" +
                        "-[r:HAS_ACCESS]->(m:Resource{tenant_id:$tenantId})-[l:HAS_CHILD_RESOURCE*]->  " +
                        "(p:Resource{tenant_id:$tenantId}) " +
                        "where r.permission_type=$permissionType   and m.name=$resourceName  or p.name=$resourceName return m,p";

        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("username", username);
        parameterMap.put("permissionType", permissionType);
        parameterMap.put("resourceName", resourceName);
        parameterMap.put("tenantId", tenantId);
        Iterable<Map<String, Object>> mapIterable = super.execute(query, parameterMap);
        List<Resource> resourceList = new ArrayList<>();
        mapIterable.forEach(map -> {
            if (!map.isEmpty()) {
                map.forEach((key, val) -> {
                    if (((Resource) val).getName().equals(resourceName)) {
                        resourceList.add((Resource) val);
                    }
                });
            }
        });
        return resourceList;
    }

    @Override
    public List<Resource> find(Resource resource) {
        String query =
                "match (n:Resource) where n.tenant_id=$tenantId and n.name=$resourceName return n";
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("resourceName", resource.getName());
        parameterMap.put("tenantId", resource.getTenantId());
        Iterable<Map<String, Object>> mapIterable = super.execute(query, parameterMap);
        List<Resource> resourceList = new ArrayList<>();
        mapIterable.forEach(map -> {
            if (!map.isEmpty()) {
                map.forEach((key, val) -> {
                    if (((Resource) val).getName().equals(resource.getName())) {
                        resourceList.add((Resource) val);
                    }
                });
            }
        });
        return resourceList;
    }
}
