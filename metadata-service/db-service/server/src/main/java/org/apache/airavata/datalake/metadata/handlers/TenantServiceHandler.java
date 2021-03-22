package org.apache.airavata.datalake.metadata.handlers;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators.TenantServiceImpl;
import org.apache.airavata.datalake.metadata.parsers.TenantParser;
import org.apache.airavata.datalake.metadata.service.Tenant;
import org.apache.airavata.datalake.metadata.service.TenantMetadataAPIRequest;
import org.apache.airavata.datalake.metadata.service.TenantMetadataAPIResponse;
import org.apache.airavata.datalake.metadata.service.TenantMetadataServiceGrpc;
import org.dozer.DozerBeanMapper;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
                    (org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant)
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
                          StreamObserver<Tenant> responseObserver) {
        try {


        } catch (Exception ex) {

        }
    }

    @Override
    public void updateTenant(TenantMetadataAPIRequest request,
                             StreamObserver<TenantMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {

        }
    }

    @Override
    public void deleteTenant(TenantMetadataAPIRequest request,
                             StreamObserver<TenantMetadataAPIResponse> responseObserver) {
        try {


        } catch (Exception ex) {

        }
    }
}
