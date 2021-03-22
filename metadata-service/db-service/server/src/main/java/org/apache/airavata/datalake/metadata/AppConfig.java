package org.apache.airavata.datalake.metadata;

import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators.ResourceServiceImpl;
import org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators.SearchOperator;
import org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators.TenantServiceImpl;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User;
import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


@Configuration
public class AppConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    private Connector connector;


    @Bean
    public DozerBeanMapper dozerBeanMapper() {

        DozerBeanMapper mapper = new DozerBeanMapper();

        BeanMappingBuilder tenantMapping = new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(org.apache.airavata.datalake.metadata.service.Tenant.class, Tenant.class);
            }
        };

        BeanMappingBuilder groupMapping = new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(org.apache.airavata.datalake.metadata.service.Group.class, Group.class);
            }
        };

        BeanMappingBuilder resourceMapping = new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(org.apache.airavata.datalake.metadata.service.Resource.class, Resource.class);
            }
        };

        BeanMappingBuilder userMapping = new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(org.apache.airavata.datalake.metadata.service.User.class, User.class);
            }
        };

        mapper.addMapping(tenantMapping);
        mapper.addMapping(groupMapping);
        mapper.addMapping(resourceMapping);
        mapper.addMapping(userMapping);

        return mapper;
    }


    @Bean
    Tenant getTenant() {
        LOGGER.info("Calling get tenant############");

        Tenant tenant = new Tenant();
        tenant.setTenantId("123456789");
        tenant.setName("Tenant");

        User user = new User();
        user.setFirstName("UserA");
        user.setUsername("user_a");

        User user1 = new User();
        user1.setFirstName("UserB");
        user1.setUsername("user_b");

        User user2 = new User();
        user2.setFirstName("UserC");
        user2.setUsername("user_c");

        Group group = new Group();
        group.setName("g1");

        Group group2 = new Group();
        group2.setName("g2");

        Group group3 = new Group();
        group3.setName("g3");

        group.addChildGroup(group2, 0, 0, null);
        group2.addChildGroup(group3, 0, 0, null);

        Resource resource = new Resource();
        resource.setName("R1");

        Resource resource1 = new Resource();
        resource1.setName("R2");

        Resource resource2 = new Resource();
        resource2.setName("R3");

        Resource resource3 = new Resource();
        resource3.setName("R4");

        group.addChildUser(user, "ADMIN", 0, 0, null);
        resource.addChildResource(resource1, 0, 0, null);
        resource.shareWithAUser(user, "READ", 0, 0, null);

        group2.addChildUser(user1, "ADMIN", 0, 0, null);
        group3.addChildUser(user2, "ADMIN", 0, 0, null);

        resource1.shareWithAGroup(group2, "WRITE", 0, 0, null);
        resource2.shareWithAGroup(group3, "WRITE", 0, 0, null);

        tenant.add(user, 0, 0, null);
        tenant.add(group, 0, 0, null);
        tenant.add(resource, 0, 0, null);

        TenantServiceImpl tenantService = new TenantServiceImpl(connector);
        tenantService.createOrUpdate(tenant);

        Filter filter = new Filter("name", ComparisonOperator.EQUALS, "R3");

        ResourceServiceImpl resourceService = new ResourceServiceImpl(connector);
        SearchOperator searchOperator = new SearchOperator();
        searchOperator.setKey("name");
        searchOperator.setValue("R2");
        searchOperator.setComparisonOperator(ComparisonOperator.EQUALS);
        List searchList = new ArrayList<>();
        searchList.add(searchOperator);
        List<Resource> collections = (List<Resource>) resourceService.search(searchList);
        LOGGER.info("Size", collections.size());
        for (Resource collection : collections) {
            LOGGER.info("#############" + collection.getName() + "Created At" + collection.getCreatedAt());
        }


        return tenant;
    }


}
