package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource;

import java.util.List;

public interface ResourceService {

    public boolean hasAccess(String username, String resourceName, String permissionType, String tenantId);

    public List<Resource> findResources(String username, String resourceName, String permissionType, String tenantId);


}
