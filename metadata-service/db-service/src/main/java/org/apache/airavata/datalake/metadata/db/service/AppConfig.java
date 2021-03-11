package org.apache.airavata.datalake.metadata.db.service;

import org.apache.airavata.datalake.metadata.db.service.backend.Connector;
import org.apache.airavata.datalake.metadata.db.service.backend.neo4j.Neo4JConnector;
import org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.nodes.Group;
import org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.nodes.Resource;
import org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.nodes.Tenant;
import org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.nodes.User;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AppConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    private Connector connector;


    @Bean
    Tenant getTenant() {
        LOGGER.info("Calling get tenant############");
        Tenant tenant = new Tenant();
        tenant.setTenantId("123456789");
        tenant.setName("Tenant");

        User user = new User();
        user.setFirstName("UserA");
        user.setUserName("user_a");

        User user1 = new User();
        user1.setFirstName("UserB");
        user1.setUserName("user_b");

        User user2 = new User();
        user2.setFirstName("UserC");
        user2.setUserName("user_c");

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

        Session session = ((Neo4JConnector) connector).openConnection();
        session.save(tenant);

        Filter filter = new Filter("name", ComparisonOperator.EQUALS, "R3");

//        Collection<Resource> resources = session.loadAll(Resource.class, filter, 1);
//        resources.stream().forEach(t -> {
//            LOGGER.info("Resources " + t.getName());
//            t.addChildResource(resource3, 0, 0, null);
//            Resource resource4 = (Resource) session.load(Resource.class, new Long(13));
//            session.delete(resource4);
//            session.save(t);
//        });


        return tenant;

    }


}
