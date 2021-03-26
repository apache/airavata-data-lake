package org.apache.airavata.drms.api;

import io.grpc.ServerInterceptor;
import org.apache.airavata.drms.api.interceptors.Authenticator;
import org.apache.airavata.drms.api.interceptors.InterceptorPipelineExecutor;
import org.apache.airavata.drms.api.interceptors.ServiceInterceptor;
import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.custos.clients.CustosClientProvider;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Stack;

@Configuration
public class Config {

    @org.springframework.beans.factory.annotation.Value("${neo4j.server.uri}")
    public String neo4jServerUri;

    @org.springframework.beans.factory.annotation.Value("${neo4j.server.user}")
    public String neo4jServerUser;

    @org.springframework.beans.factory.annotation.Value("${neo4j.server.password}")
    public String neo4jServerPassword;

    @Value("custos.id")
    private String custosId;

    @Value("custos.secret")
    private String custosSec;

    @Value("custos.host")
    private String custosHost;

    @Value("custos.port")
    private int custosPort;

    @Bean
    public Neo4JConnector neo4JConnector() {
        return new Neo4JConnector(neo4jServerUri, neo4jServerUser, neo4jServerPassword);
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
