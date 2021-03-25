package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

public interface ResourceService {

    public boolean hasAccess(String username, String resourceName, String permissionType, String tenantId);


}
