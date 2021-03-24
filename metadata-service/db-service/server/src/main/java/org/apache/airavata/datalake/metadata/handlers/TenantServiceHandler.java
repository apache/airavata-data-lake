package org.apache.airavata.datalake.metadata.handlers;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators.TenantServiceImpl;
import org.apache.airavata.datalake.metadata.parsers.TenantParser;
import org.apache.airavata.datalake.metadata.service.*;
import org.dozer.DozerBeanMapper;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@GRpcService
public class TenantServiceHandler extends TenantMetadataServiceGrpc.TenantMetadataServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenantServiceHandler.class);

    @Autowired
    private DozerBeanMapper dozerBeanMapper;

    @Autowired
    private TenantParser tenantParser;

    @Autowired
    private Connector connector;


    @Override
    public void createTenant(TenantMetadataAPIRequest request,
                             StreamObserver<TenantMetadataAPIResponse> responseObserver) {
        try {
            Tenant tenant = request.getTenant();
            org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant parsedTenant =
                    tenantParser.parse(tenant);

            TenantServiceImpl tenantService = new TenantServiceImpl(connector);
            tenantService.createOrUpdate(parsedTenant);
            TenantMetadataAPIResponse response = TenantMetadataAPIResponse.newBuilder().setStatus(true).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Exception occurred while creating tenant " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void getTenant(TenantMetadataAPIRequest request,
                          StreamObserver<FindTenantResponse> responseObserver) {
        try {
            Tenant tenant = request.getTenant();
            org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant parsedTenant = tenantParser
                    .parse(tenant);

            TenantServiceImpl tenantService = new TenantServiceImpl(connector);
            List<org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant> tenantList =
                    tenantService.find(parsedTenant);

            if (tenantList.isEmpty()) {
                responseObserver.onCompleted();
            }

            List<Tenant> tenants = tenantList.stream().map(t -> {
                return tenantParser.parse(t);
            }).collect(Collectors.toList());

            FindTenantResponse response = FindTenantResponse.newBuilder().addAllTenants(tenants).build();
            responseObserver.onNext(response);

        } catch (Exception ex) {
            String msg = "Exception occurred while fetching tenant " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void updateTenant(TenantMetadataAPIRequest request,
                             StreamObserver<TenantMetadataAPIResponse> responseObserver) {
        try {

            TenantServiceImpl tenantService = new TenantServiceImpl(connector);
            Tenant tenant = request.getTenant();
            org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant parsedTenant =
                    tenantParser
                            .parseAndMerge(tenant);


            List<org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant> tenantList =
                    tenantService.find(parsedTenant);

            if (tenantList.isEmpty()) {
                responseObserver.onCompleted();
            }

            tenantService.createOrUpdate(parsedTenant);
            TenantMetadataAPIResponse response = TenantMetadataAPIResponse.newBuilder().setStatus(true).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();


        } catch (Exception ex) {
            String msg = "Exception occurred while updating tenant " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }

    @Override
    public void deleteTenant(TenantMetadataAPIRequest request,
                             StreamObserver<TenantMetadataAPIResponse> responseObserver) {
        try {
            Tenant tenant = request.getTenant();
            org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant parsedTenant = tenantParser
                    .parse(tenant);

            TenantServiceImpl tenantService = new TenantServiceImpl(connector);
            List<org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant> tenantList =
                    tenantService.find(parsedTenant);

            if (tenantList.isEmpty()) {
                responseObserver.onCompleted();
            }

            tenantService.delete(tenantList.get(0).getId());
            TenantMetadataAPIResponse response = TenantMetadataAPIResponse.newBuilder().setStatus(true).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();


        } catch (Exception ex) {
            String msg = "Exception occurred while deleting tenant " + ex;
            LOGGER.error(msg);
            responseObserver.onError(new Exception(msg));
        }
    }
}
