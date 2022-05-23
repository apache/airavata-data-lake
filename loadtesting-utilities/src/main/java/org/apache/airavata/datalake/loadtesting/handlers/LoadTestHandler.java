package org.apache.airavata.datalake.loadtesting.handlers;

import com.opencsv.CSVWriter;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.MetadataUtils;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.custos.clients.core.ClientUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;


@RestController
public class LoadTestHandler {

    @Value("${result.path}")
    private String resultPath;

    @Value("${remote.service.host}")
    private String serviceHost;

    @Value("${remote.service.port}")
    private int servicePort;

    @RequestMapping(value = "/testFetchResource", method = RequestMethod.GET)
    @ResponseBody
    public String testFetchResource(@RequestParam("username") String username, @RequestParam("tenantId") String tenantId,
                                    @RequestParam("entityId") String entityId,
                                    @RequestParam("totalIterations") int iterations,
                                    @RequestParam("reportChunkSize") int reportChunkSize,
                                    @RequestParam("filePath") String filePath) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serviceHost, servicePort).usePlaintext().build();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceGrpc.newBlockingStub(channel);

        ResourceFetchRequest request = ResourceFetchRequest.newBuilder()
                .setResourceId(entityId)
                .setAuthToken(DRMSServiceAuthToken.newBuilder()
                        .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                                .setTenantId(tenantId)
                                .setUsername(username)
                                .build())).build();

        int successRequests = 0;
        int failureRequests = 0;
        long sum = 0;

        Map<String, String> latencyMap = new HashMap<>();

        int chunkSize = 1;

        for (int i = 0; i < iterations; i++) {
            try {
                long beginLatency = System.currentTimeMillis();
                ResourceFetchResponse response = resourceClient.fetchResource(request);
                long endLatency = System.currentTimeMillis();

                long diff = endLatency - beginLatency;
                latencyMap.put(String.valueOf(i), String.valueOf(diff));
                sum += diff;
                String resourceId = response.getResource().getResourceId();
                if (!resourceId.isEmpty()) {
                    successRequests++;
                }
            } catch (Exception ex) {
                failureRequests++;
            }

            if (chunkSize % reportChunkSize == 0) {
                long avgSuccessRate = (successRequests / chunkSize) * 100;
                long avgFailureRare = (failureRequests / chunkSize) * 100;

                writeToAFile(latencyMap, avgSuccessRate, avgFailureRare, filePath, "testFetchResource");
                latencyMap.clear();
                successRequests = 0;
                failureRequests = 0;
                chunkSize = 1;
            } else {
                chunkSize++;
            }
        }
        return "load test completed";
    }


    @RequestMapping(value = "/testSearchResource", method = RequestMethod.GET)
    @ResponseBody
    public String testSearchResource(@RequestParam("username") String username,
                                     @RequestParam("tenantId") String tenantId,
                                     @RequestParam("totalIterations") int iterations,
                                     @RequestParam("reportChunkSize") int reportChunkSize,
                                     @RequestParam("filePath") String filePath,
                                     @RequestParam("depth") int depth,
                                     @RequestParam("type") String type,
                                     @RequestParam("token") String token) {


        ManagedChannel channel = ManagedChannelBuilder.forAddress(serviceHost, servicePort).usePlaintext().build();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceGrpc.newBlockingStub(channel);
        ResourceSearchRequest request = null;
        if (token.isEmpty()) {
            request = ResourceSearchRequest
                    .newBuilder()
                    .setType(type)
                    .setDepth(depth)
                    .setAuthToken(DRMSServiceAuthToken.newBuilder()
                            .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                                    .setTenantId(tenantId)
                                    .setUsername(username)
                                    .build()))
                    .build();
        } else {
            request = ResourceSearchRequest
                    .newBuilder()
                    .setType(type)
                    .setDepth(depth)
                    .build();
            resourceClient =  MetadataUtils.attachHeaders(resourceClient, ClientUtils.getAuthorizationHeader(token));

        }


        int successRequests = 0;
        int failureRequests = 0;
        long sum = 0;

        Map<String, String> latencyMap = new HashMap<>();

        int chunkSize = 1;

        for (int i = 0; i < iterations; i++) {
            try {
                long beginLatency = System.currentTimeMillis();
                ResourceSearchResponse response = resourceClient.searchResource(request);
                long endLatency = System.currentTimeMillis();

                long diff = endLatency - beginLatency;
                latencyMap.put(String.valueOf(i), String.valueOf(diff));
                sum += diff;
                if (response.getResourcesList() != null) {
                    successRequests++;
                }
            } catch (Exception ex) {
                failureRequests++;
            }

            if (chunkSize % reportChunkSize == 0) {
                long avgSuccessRate = (successRequests / chunkSize) * 100;
                long avgFailureRare = (failureRequests / chunkSize) * 100;

                writeToAFile(latencyMap, avgSuccessRate, avgFailureRare, filePath, "testSearchResource");
                latencyMap.clear();
                successRequests = 0;
                failureRequests = 0;
                chunkSize = 1;
            } else {
                chunkSize++;
            }
        }
        return "load test completed";

    }


    @RequestMapping(value = "/testFetchChildResource", method = RequestMethod.GET)
    @ResponseBody
    public String testFetchChildResource(@RequestParam("username") String username,
                                         @RequestParam("tenantId") String tenantId,
                                         @RequestParam("totalIterations") int iterations,
                                         @RequestParam("reportChunkSize") int reportChunkSize,
                                         @RequestParam("filePath") String filePath,
                                         @RequestParam("entityId") String resourceId,
                                         @RequestParam("depth") int depth,
                                         @RequestParam("type") String type) {


        ManagedChannel channel = ManagedChannelBuilder.forAddress(serviceHost, servicePort).usePlaintext().build();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceGrpc.newBlockingStub(channel);

        ChildResourceFetchRequest request = ChildResourceFetchRequest
                .newBuilder()
                .setType(type)
                .setDepth(depth)
                .setResourceId(resourceId)
                .setAuthToken(DRMSServiceAuthToken.newBuilder()
                        .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                                .setTenantId(tenantId)
                                .setUsername(username)
                                .build()))
                .build();

        int successRequests = 0;
        int failureRequests = 0;
        long sum = 0;

        Map<String, String> latencyMap = new HashMap<>();

        int chunkSize = 1;

        for (int i = 0; i < iterations; i++) {
            try {
                long beginLatency = System.currentTimeMillis();
                ChildResourceFetchResponse response = resourceClient.fetchChildResources(request);
                long endLatency = System.currentTimeMillis();

                long diff = endLatency - beginLatency;
                latencyMap.put(String.valueOf(i), String.valueOf(diff));
                sum += diff;
                if (!response.getResourcesList().isEmpty()) {
                    successRequests++;
                }
            } catch (Exception ex) {
                failureRequests++;
            }

            if (chunkSize % reportChunkSize == 0) {
                long avgSuccessRate = (successRequests / chunkSize) * 100;
                long avgFailureRare = (failureRequests / chunkSize) * 100;

                writeToAFile(latencyMap, avgSuccessRate, avgFailureRare, filePath, "testFetchChildResource");
                latencyMap.clear();
                successRequests = 0;
                failureRequests = 0;
                chunkSize = 1;
            } else {
                chunkSize++;
            }
        }
        return "load test completed";

    }

    @RequestMapping(value = "/testResourceCreate", method = RequestMethod.GET)
    @ResponseBody
    public String testCreateResource(@RequestParam("username") String username,
                                     @RequestParam("tenantId") String tenantId,
                                     @RequestParam("totalIterations") int iterations,
                                     @RequestParam("reportChunkSize") int reportChunkSize,
                                     @RequestParam("filePath") String filePath,
                                     @RequestParam("entityId") String resourceId,
                                     @RequestParam("entityName") String resourceName,
                                     @RequestParam("type") String type) {


        ManagedChannel channel = ManagedChannelBuilder.forAddress(serviceHost, servicePort).usePlaintext().build();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceGrpc.newBlockingStub(channel);


        int successRequests = 0;
        int failureRequests = 0;
        long sum = 0;

        Map<String, String> latencyMap = new HashMap<>();

        int chunkSize = 1;

        for (int i = 0; i < iterations; i++) {
            try {
                long beginLatency = System.currentTimeMillis();
                ResourceCreateRequest request = ResourceCreateRequest
                        .newBuilder()
                        .setAuthToken(DRMSServiceAuthToken.newBuilder()
                                .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                                        .setTenantId(tenantId)
                                        .setUsername(username)
                                        .build()))
                        .setResource(GenericResource.newBuilder()
                                .setResourceName(resourceName + "_" + i)
                                .setResourceId(resourceId + "_" + i)
                                .setType(type)
                                .build())
                        .build();
                ResourceCreateResponse response = resourceClient.createResource(request);
                long endLatency = System.currentTimeMillis();

                long diff = endLatency - beginLatency;
                latencyMap.put(String.valueOf(i), String.valueOf(diff));
                sum += diff;
                if (!response.getResource().getResourceId().isEmpty()) {
                    successRequests++;
                }
            } catch (Exception ex) {
                failureRequests++;
            }

            if (chunkSize % reportChunkSize == 0) {
                long avgSuccessRate = (successRequests / chunkSize) * 100;
                long avgFailureRare = (failureRequests / chunkSize) * 100;

                writeToAFile(latencyMap, avgSuccessRate, avgFailureRare, filePath, "testResourceCreateRequest");
                latencyMap.clear();
                successRequests = 0;
                failureRequests = 0;
                chunkSize = 1;
            } else {
                chunkSize++;
            }
        }
        return "load test completed";


    }


    @RequestMapping(value = "/testFetchMetadata", method = RequestMethod.GET)
    @ResponseBody
    public String testFetchMetadata(@RequestParam("username") String username,
                                    @RequestParam("tenantId") String tenantId,
                                    @RequestParam("totalIterations") int iterations,
                                    @RequestParam("reportChunkSize") int reportChunkSize,
                                    @RequestParam("filePath") String filePath,
                                    @RequestParam("entityId") String resourceId) {


        ManagedChannel channel = ManagedChannelBuilder.forAddress(serviceHost, servicePort).usePlaintext().build();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceGrpc.newBlockingStub(channel);


        int successRequests = 0;
        int failureRequests = 0;
        long sum = 0;

        Map<String, String> latencyMap = new HashMap<>();

        int chunkSize = 1;

        for (int i = 0; i < iterations; i++) {
            try {
                long beginLatency = System.currentTimeMillis();
                FetchResourceMetadataRequest request = FetchResourceMetadataRequest
                        .newBuilder()
                        .setAuthToken(DRMSServiceAuthToken.newBuilder()
                                .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                                        .setTenantId(tenantId)
                                        .setUsername(username)
                                        .build()))
                        .setResourceId(resourceId)
                        .build();
                FetchResourceMetadataResponse response = resourceClient.fetchResourceMetadata(request);
                long endLatency = System.currentTimeMillis();

                long diff = endLatency - beginLatency;
                latencyMap.put(String.valueOf(i), String.valueOf(diff));
                sum += diff;
                if (response.getMetadataCount() != 0) {
                    successRequests++;
                }
            } catch (Exception ex) {
                failureRequests++;
            }

            if (chunkSize % reportChunkSize == 0) {
                long avgSuccessRate = (successRequests / chunkSize) * 100;
                long avgFailureRare = (failureRequests / chunkSize) * 100;

                writeToAFile(latencyMap, avgSuccessRate, avgFailureRare, filePath, "testResourceCreateRequest");
                latencyMap.clear();
                successRequests = 0;
                failureRequests = 0;
                chunkSize = 1;
            } else {
                chunkSize++;
            }
        }
        return "load test completed";


    }


    private void writeToAFile(Map<String, String> valuesMap, long avgSuccessRate, long avgFailureRate, String resultPath,
                              String methodName) {

        try {
            String latecnyPath = resultPath + File.pathSeparator + methodName + "-latency.csv";
            String ratePath = resultPath + File.pathSeparator + methodName + "-rate.csv";

            FileWriter latency = new FileWriter(latecnyPath, true);

            CSVWriter writer = new CSVWriter(latency);
            valuesMap.values().forEach(value -> {
                writer.writeNext(new String[]{value});
            });

            // closing writer con
            writer.close();


            FileWriter rate = new FileWriter(ratePath, true);

            CSVWriter rateWriter = new CSVWriter(rate);
            rateWriter.writeNext(new String[]{String.valueOf(avgSuccessRate), String.valueOf(avgFailureRate)});
            // closing writer con
            rateWriter.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
