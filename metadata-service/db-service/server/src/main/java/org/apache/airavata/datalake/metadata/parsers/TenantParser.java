package org.apache.airavata.datalake.metadata.parsers;

import com.google.protobuf.GeneratedMessageV3;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Entity;
import org.apache.airavata.datalake.metadata.service.Group;
import org.apache.airavata.datalake.metadata.service.Resource;
import org.apache.airavata.datalake.metadata.service.User;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TenantParser implements Parser {
    private static Logger LOGGER = LoggerFactory.getLogger(TenantParser.class);

    @Autowired
    private DozerBeanMapper dozerBeanMapper;

    @Autowired
    private GroupParser groupParser;

    @Autowired
    private UserParser userParser;

    @Autowired
    private ResourceParser resourceParser;

    @Override
    public Entity parse(GeneratedMessageV3 entity, Entity parentEntity, ExecutionContext executionContext) {
        if (entity instanceof org.apache.airavata.datalake.metadata.service.Tenant) {

            org.apache.airavata.datalake.metadata.service.Tenant tenant =
                    (org.apache.airavata.datalake.metadata.service.Tenant) entity;

            List<Group> groups = tenant.getGroupsList();
            List<User> users = tenant.getUsersList();
            List<Resource> resources = tenant.getResourcesList();

            org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant neo4JTenant =
                    dozerBeanMapper.map(tenant,
                            org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant.class);
            neo4JTenant.setPrimaryExternalKey(neo4JTenant.getTenantId());
            executionContext.addNeo4JConvertedModels(neo4JTenant.getSearchableId(),neo4JTenant);

            if (!groups.isEmpty()) {
                groups.stream().forEach(group -> {
                    org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group neo4JGr =
                            (org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group)
                                    groupParser.parse(group, null, executionContext);
                    executionContext.addNeo4JConvertedModels(neo4JGr.getSearchableId(),neo4JGr);
                    neo4JTenant.add(neo4JGr, tenant.getCreatedAt() != 0 ? tenant.getCreatedAt() : System.currentTimeMillis(),
                            tenant.getLastModifiedAt() != 0 ? tenant.getLastModifiedAt() : System.currentTimeMillis(),
                            null);
                });
            }

            if (!users.isEmpty()) {
                users.stream().forEach(user -> {
                    org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User usr =
                            (org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User)
                                    userParser.parse(user, null, executionContext);
                    executionContext.addNeo4JConvertedModels(usr.getSearchableId(),usr);
                    neo4JTenant.add(usr, tenant.getCreatedAt() != 0 ? tenant.getCreatedAt() : System.currentTimeMillis(),
                            tenant.getLastModifiedAt() != 0 ? tenant.getLastModifiedAt() : System.currentTimeMillis(),
                            null);
                });
            }

            if (!resources.isEmpty()) {
                resources.stream().forEach(resource -> {
                    org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource neo4JResource =
                            (org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource)
                                    resourceParser.parse(resource, null, executionContext);
                    executionContext.addNeo4JConvertedModels(neo4JResource.getSearchableId(),neo4JResource);
                    neo4JTenant.add(neo4JResource, tenant.getCreatedAt() != 0 ? tenant.getCreatedAt() : System.currentTimeMillis(),
                            tenant.getLastModifiedAt() != 0 ? tenant.getLastModifiedAt() : System.currentTimeMillis(),
                            null);
                });
            }
            return neo4JTenant;
        } else {
            String msg = "Wrong entity type detected for parser Tenant Parser, Expected Tenant";
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
    }

    @Override
    public Entity parse(GeneratedMessageV3 entity, ExecutionContext executionContext) {
        return this.parse(entity, null, executionContext);
    }

    @Override
    public Entity parse(GeneratedMessageV3 entity) {
        return this.parse(entity, null, new ExecutionContext());
    }
}
