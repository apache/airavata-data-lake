package org.apache.airavata.datalake.metadata.parsers;

import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant;
import org.apache.airavata.datalake.metadata.service.Group;
import org.apache.airavata.datalake.metadata.service.Resource;
import org.apache.airavata.datalake.metadata.service.User;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TenantParser {
    private static Logger LOGGER = LoggerFactory.getLogger(TenantParser.class);

    @Autowired
    private DozerBeanMapper dozerBeanMapper;

    @Autowired
    private GroupParser groupParser;

    @Autowired
    private UserParser userParser;

    @Autowired
    private ResourceParser resourceParser;


    public Tenant parseTenant(org.apache.airavata.datalake.metadata.service.Tenant tenant) {
        List<Group> groups = tenant.getGroupsList();
        List<User> users = tenant.getUsersList();
        List<Resource> resources = tenant.getResourcesList();


        org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant neo4JTenant =
                dozerBeanMapper.map(tenant, org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant.class);

        if (!groups.isEmpty()) {
            groups.stream().forEach(group -> {
                org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group neo4JGr =
                        groupParser.parseGroup(group, Optional.empty());
                neo4JTenant.add(neo4JGr, tenant.getCreatedAt() != 0 ? tenant.getCreatedAt() : System.currentTimeMillis(),
                        tenant.getLastModifiedAt() != 0 ? tenant.getLastModifiedAt() : System.currentTimeMillis(),
                        null);

            });
        }

        if (!users.isEmpty()) {
            users.stream().forEach(user -> {
                org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User usr =
                        userParser.parseUser(user);
                neo4JTenant.add(usr, tenant.getCreatedAt() != 0 ? tenant.getCreatedAt() : System.currentTimeMillis(),
                        tenant.getLastModifiedAt() != 0 ? tenant.getLastModifiedAt() : System.currentTimeMillis(),
                        null);

            });
        }

        if (!resources.isEmpty()) {
            resources.stream().forEach(resource -> {
                org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource neo4JResource =
                        resourceParser.parseResource(resource, Optional.empty());
                neo4JTenant.add(neo4JResource, tenant.getCreatedAt() != 0 ? tenant.getCreatedAt() : System.currentTimeMillis(),
                        tenant.getLastModifiedAt() != 0 ? tenant.getLastModifiedAt() : System.currentTimeMillis(),
                        null);

            });
        }
        return neo4JTenant;

    }


}
