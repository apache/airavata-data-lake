package org.apache.airavata.datalake.metadata;

import io.grpc.ServerInterceptor;
import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators.TenantServiceImpl;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Group;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Resource;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User;
import org.apache.airavata.datalake.metadata.interceptors.Authenticator;
import org.apache.airavata.datalake.metadata.interceptors.InterceptorPipelineExecutor;
import org.apache.airavata.datalake.metadata.interceptors.ServiceInterceptor;
import org.apache.custos.clients.CustosClientProvider;
import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Stack;


@Configuration
public class AppConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    private Connector connector;

    @Value("${custos.id}")
    private String custosId;

    @Value("${custos.secret}")
    private String custosSec;

    @Value("${custos.host}")
    private String custosHost;

    @Value("${custos.port}")
    private int custosPort;


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
    public Stack<ServiceInterceptor> getInterceptorSet(Authenticator authInterceptor) {
        Stack<ServiceInterceptor> interceptors = new Stack<>();
        interceptors.add(authInterceptor);
        return interceptors;
    }


    @Bean
    @GRpcGlobalInterceptor
    public ServerInterceptor validationInterceptor(Stack<ServiceInterceptor> integrationServiceInterceptors) {
        return new InterceptorPipelineExecutor(integrationServiceInterceptors);
    }


    @Bean
    public CustosClientProvider custosClientsFactory() {
        return new CustosClientProvider.Builder().setServerHost(custosHost)
                .setServerPort(custosPort)
                .setClientId(custosId)
                .setClientSec(custosSec).build();
    }
}
