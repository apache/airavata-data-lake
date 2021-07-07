/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.airavata.drms.api.handlers;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.airavata.drms.api.utils.CustosUtils;
import org.apache.airavata.drms.api.utils.Utils;
import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.airavata.drms.core.constants.StoragePreferenceConstants;
import org.apache.airavata.drms.core.deserializer.GenericResourceDeserializer;
import org.apache.airavata.drms.core.serializer.GenericResourceSerializer;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.sharing.service.Entity;
import org.json.JSONObject;
import org.lognet.springboot.grpc.GRpcService;
import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@GRpcService
public class ResourceServiceHandler extends ResourceServiceGrpc.ResourceServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceHandler.class);

    @Autowired
    private Neo4JConnector neo4JConnector;

    @Autowired
    private CustosClientProvider custosClientProvider;


    private static final String DATA_LAKE_JSON_IDENTIFIER = "DATA_LAKE_METADATA_NODE_JSON_IDENTIFIER";

    @Override
    public void fetchResource(ResourceFetchRequest request, StreamObserver<ResourceFetchResponse> responseObserver) {

        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            String resourceId = request.getResourceId();
            String type = request.getType();
            if (type == null || type.isEmpty()) {
                type = "";
            } else {
                type = ":" + type;
            }
            Map<String, Object> userProps = new HashMap<>();
            userProps.put("username", callUser.getUsername());
            userProps.put("tenantId", callUser.getTenantId());
            userProps.put("entityId", resourceId);


            String query = " MATCH (u:User),  (r" + type + ") where u.username = $username AND u.tenantId = $tenantId AND " +
                    " r.entityId = $entityId AND r.tenantId = $tenantId" +
                    " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g:Group)<-[:MEMBER_OF]-(u)" +
                    " return case when  exists((u)<-[:SHARED_WITH]-(r)) OR  exists((g)<-[:SHARED_WITH]-(r)) OR   " +
                    "exists((cg)<-[:SHARED_WITH]-(r)) then r  else NULL end as value";


            List<Record> records = this.neo4JConnector.searchNodes(userProps, query);
            try {
                List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
                ResourceFetchResponse.Builder builder = ResourceFetchResponse.newBuilder();
                if (!genericResourceList.isEmpty()) {
                    builder.setResource(genericResourceList.get(0));
                }
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();

            } catch (Exception e) {
                logger.error("Errored while searching generic child resources; Message: {}", e.getMessage(), e);
                String msg = "Errored while searching generic child resources " + e.getMessage();
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }

        } catch (
                Exception ex) {
            logger.error("Error occurred while fetching child resource {}", request.getResourceId());
            String msg = "Error occurred while creating resource with id" + request.getResourceId();
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }

    }

    @Override
    public void createResource(ResourceCreateRequest request, StreamObserver<ResourceCreateResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            String type = request.getResource().getType();

            Map<String, Object> userProps = new HashMap<>();
            userProps.put("username", callUser.getUsername());
            userProps.put("tenantId", callUser.getTenantId());

            String parentId = request.getResource().getParentId();

            String entityId = request.getResource().getResourceId();
            Map<String, Object> serializedMap = GenericResourceSerializer.serializeToMap(request.getResource());
            Optional<Entity> exEntity = CustosUtils.mergeResourceEntity(custosClientProvider, callUser.getTenantId(),
                    parentId, type, entityId,
                    request.getResource().getResourceName(), request.getResource().getResourceName(),
                    callUser.getUsername());

            if (exEntity.isPresent()) {
                serializedMap.put("description", exEntity.get().getDescription());
                serializedMap.put("resourceName", exEntity.get().getName());
                serializedMap.put("createdTime", String.valueOf(exEntity.get().getCreatedAt()));
                serializedMap.put("tenantId", callUser.getTenantId());
                serializedMap.put("entityId", exEntity.get().getId());
                serializedMap.put("entityType", exEntity.get().getType());
                serializedMap.put("lastModifiedTime", exEntity.get().getCreatedAt());
                serializedMap.put("owner", exEntity.get().getOwnerId());

                if (!parentId.isEmpty()) {
                    this.neo4JConnector.mergeNodesWithParentChildRelationShip(serializedMap, new HashMap<>(),
                            request.getResource().getType(), StoragePreferenceConstants.STORAGE_PREFERENCE_LABEL,
                            callUser.getUsername(), entityId, parentId, callUser.getTenantId());
                } else {
                    this.neo4JConnector.mergeNode(serializedMap, request.getResource().getType(),
                            callUser.getUsername(), entityId, callUser.getTenantId());
                }
            } else {
                logger.error("Error occurred while creating resource entity in Custos {}", request.getResource().getResourceId());
                String msg = "Error occurred while creating resource entity in Custos with id"
                        + request.getResource().getResourceId();
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
                return;
            }
            Map<String, Object> exProps = new HashMap<>();
            exProps.put("username", callUser.getUsername());
            exProps.put("tenantId", callUser.getTenantId());
            exProps.put("entityId", exEntity.get().getId());

            String query = " MATCH (u:User),  (r:" + type + ") where u.username = $username AND u.tenantId = $tenantId AND " +
                    " r.entityId = $entityId AND r.tenantId = $tenantId" +
                    " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g:Group)<-[:MEMBER_OF]-(u)" +
                    " return case when  exists((u)<-[:SHARED_WITH]-(r)) OR  exists((g)<-[:SHARED_WITH]-(r)) OR   " +
                    "exists((cg)<-[:SHARED_WITH]-(r)) then r  else NULL end as value";


            List<Record> records = this.neo4JConnector.searchNodes(exProps, query);

            List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
            GenericResource genericResource = genericResourceList.get(0);
            if (genericResource.getPropertiesMap().containsKey("name")) {
                genericResource = genericResource.toBuilder()
                        .setResourceName(genericResource.getPropertiesMap().get("name")).build();
            } else if (genericResource.getPropertiesMap().containsKey("resourceName")) {
                genericResource = genericResource.toBuilder()
                        .setResourceName(genericResource.getPropertiesMap().get("resourceName")).build();
            }
            ResourceCreateResponse response = ResourceCreateResponse
                    .newBuilder()
                    .setResource(genericResource)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();


        } catch (Exception ex) {
            logger.error("Error occurred while creating resource {}", request.getResource().getResourceId());
            String msg = "Error occurred while creating resource" + ex.getMessage();
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }

    }


    @Override
    public void fetchChildResources(ChildResourceFetchRequest request,
                                    StreamObserver<ChildResourceFetchResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            String resourceId = request.getResourceId();
            String type = request.getType();
            int depth = request.getDepth();
            if (type == null || type.isEmpty()) {
                type = "";
            } else {
                type = ":" + type;
            }

            Map<String, Object> userProps = new HashMap<>();
            userProps.put("username", callUser.getUsername());
            userProps.put("tenantId", callUser.getTenantId());
            userProps.put("entityId", resourceId);

            String query = " MATCH (u:User),  (r" + type + ") where u.username = $username AND u.tenantId = $tenantId AND " +
                    " r.entityId = $entityId AND r.tenantId = $tenantId" +
                    " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g:Group)<-[:MEMBER_OF]-(u)" +
                    " OPTIONAL MATCH (u)<-[:SHARED_WITH]-(r)<-[:CHILD_OF*]-(cr)" +
                    " OPTIONAL MATCH (g)<-[:SHARED_WITH]-(r)<-[:CHILD_OF*]-(chgr)" +
                    " OPTIONAL MATCH (cg)<-[:SHARED_WITH]-(r)<-[:CHILD_OF*]-(chcgr)" +
                    " return distinct  cr, chgr, chcgr";

            if (depth == 1) {
                query = " MATCH (u:User),  (r" + type + ") where u.username = $username AND u.tenantId = $tenantId AND " +
                        " r.entityId = $entityId AND r.tenantId = $tenantId" +
                        " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g:Group)<-[:MEMBER_OF]-(u)" +
                        " OPTIONAL MATCH (u)<-[:SHARED_WITH]-(r)<-[:CHILD_OF]-(cr)" +
                        " OPTIONAL MATCH (g)<-[:SHARED_WITH]-(r)<-[:CHILD_OF]-(chgr)" +
                        " OPTIONAL MATCH (cg)<-[:SHARED_WITH]-(r)<-[:CHILD_OF]-(chcgr)" +
                        " return distinct  cr, chgr, chcgr";
            }

            List<Record> records = this.neo4JConnector.searchNodes(userProps, query);

            try {
                List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
                ChildResourceFetchResponse.Builder builder = ChildResourceFetchResponse.newBuilder();
                builder.addAllResources(genericResourceList);
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();

            } catch (Exception e) {
                logger.error("Errored while searching generic child resources; Message: {}", e.getMessage(), e);
                String msg = "Errored while searching generic child resources" + e.getMessage();
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }

        } catch (Exception ex) {
            logger.error("Error occurred while fetching child resource {}", request.getResourceId());
            responseObserver.onError(Status.INTERNAL.withDescription("Error occurred while fetching child resource"
                    + ex.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void updateResource(ResourceUpdateRequest
                                       request, StreamObserver<ResourceUpdateResponse> responseObserver) {
        super.updateResource(request, responseObserver);
    }

    @Override
    public void deletePreferenceStorage(ResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        super.deletePreferenceStorage(request, responseObserver);
    }

    @Override
    public void searchResource(ResourceSearchRequest
                                       request, StreamObserver<ResourceSearchResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            List<ResourceSearchQuery> resourceSearchQueries = request.getQueriesList();
            int depth = request.getDepth();
            String value = request.getType();
            if (value == null || value.isEmpty()) {
                logger.error("Errored while searching generic resources");
                responseObserver
                        .onError(Status.INTERNAL.withDescription("Errored while searching generic resources ")
                                .asRuntimeException());
                return;
            }

            Optional<String> metadataSearchQueryOP = Utils.getMetadataSearchQuery(resourceSearchQueries, value);
            Optional<String> ownPropertySearchQuery = Utils.getPropertySearchQuery(resourceSearchQueries, value);
            if (metadataSearchQueryOP.isPresent()) {
                String query = metadataSearchQueryOP.get();

                List<Record> records = this.neo4JConnector.searchNodes(query);
                List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);


                List<GenericResource> allowedResourceList = new ArrayList<>();

                genericResourceList.forEach(res -> {
                    try {
                        if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), res.getResourceId(), value)) {
                            allowedResourceList.add(res);
                        }
                    } catch (Exception exception) {
                        logger.error("Errored while searching generic resources");
                        responseObserver
                                .onError(Status.INTERNAL.withDescription("Errored while searching generic resources ")
                                        .asRuntimeException());
                        return;
                    }
                });

                if (ownPropertySearchQuery.isPresent()) {
                    List<Record> ownPropertySearchRecords = this.neo4JConnector.searchNodes(ownPropertySearchQuery.get());
                    List<GenericResource> genericResources = GenericResourceDeserializer.deserializeList(ownPropertySearchRecords);
                    genericResources.forEach(res -> {
                        try {
                            if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), res.getResourceId(), value)) {
                                allowedResourceList.add(res);
                            }
                        } catch (Exception exception) {
                            logger.error("Errored while searching generic resources");
                            responseObserver
                                    .onError(Status.INTERNAL.withDescription("Errored while searching generic resources ")
                                            .asRuntimeException());
                            return;
                        }
                    });

                }
                ResourceSearchResponse.Builder builder = ResourceSearchResponse.newBuilder();
                builder.addAllResources(allowedResourceList);
                responseObserver.onNext(builder.build());
            } else {
                Map<String, Object> userProps = new HashMap<>();
                userProps.put("username", callUser.getUsername());
                userProps.put("tenantId", callUser.getTenantId());

                String query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId" +
                        " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u) " +
                        " OPTIONAL MATCH (u)<-[:SHARED_WITH]-(m)<-[:CHILD_OF*]-(rm:" + value + ")" +
                        " , (r:" + value + ")-[:SHARED_WITH]->(u)" +
                        " OPTIONAL MATCH (g)<-[:SHARED_WITH]-(mg)<-[:CHILD_OF*]-(rmg:" + value + ")" +
                        " , (rg:" + value + ")-[:SHARED_WITH]->(g)" +
                        " return distinct  rm, r,rmg,rg ";

                if (depth == 1) {
                    query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId" +
                            " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u) " +
                            " OPTIONAL MATCH (r:" + value + ")-[:SHARED_WITH]->(u)" +
                            " OPTIONAL MATCH (rg:" + value + ")-[:SHARED_WITH]->(g)" +
                            " return distinct   r, rg ";
                }

                List<Record> records = this.neo4JConnector.searchNodes(userProps, query);

                List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
                ResourceSearchResponse.Builder builder = ResourceSearchResponse.newBuilder();
                builder.addAllResources(genericResourceList);
                responseObserver.onNext(builder.build());
            }
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Errored while searching generic resources; Message: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Errored while searching generic resources "
                    + e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void addChildMembership(AddChildResourcesMembershipRequest request,
                                   StreamObserver<OperationStatusResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();
            GenericResource resource = request.getParentResource();
            List<GenericResource> childResources = request.getChildResourcesList();

            List<GenericResource> allResources = new ArrayList<>();
            allResources.add(resource);
            allResources.addAll(childResources);

            //TODO: can create raise conditions please move to DB level logic
            allResources.forEach(res -> {
                try {
                    if (!hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), res.getResourceId(), res.getType())) {
                        String msg = " Don't have access to change memberships";
                        responseObserver.onError(Status.PERMISSION_DENIED.withDescription(msg).asRuntimeException());
                        return;
                    }
                } catch (Exception exception) {
                    logger.error(" Error occurred while checking for permissions: Message {} "
                            + exception.getMessage(), exception);
                    String msg = " Error occurred while checking for permissions ";
                    responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
                    return;
                }
            });


            childResources.forEach(childResource -> {
                Map<String, Object> userProps = new HashMap<>();
                userProps.put("tenantId", callUser.getTenantId());
                userProps.put("entityId", resource.getResourceId());
                userProps.put("childEntityId", childResource.getResourceId());
                String query = "MATCH  (r:" + resource.getType() + "), (cr:" + childResource.getType() + ")  where " +
                        " r.entityId = $entityId AND r.tenantId = $tenantId  AND cr.entityId = $childEntityId AND cr.tenantId = $tenantId " +
                        " MERGE (cr)-[:CHILD_OF]->(r) return r, cr";
                this.neo4JConnector.runTransactionalQuery(userProps, query);
            });

            responseObserver.onNext(OperationStatusResponse.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            String msg = " Error occurred while adding  child memberships " + e.getMessage();
            logger.error(" Error occurred while adding  child memberships: Messages {} ", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }

    }

    @Override
    public void deleteChildMembership(DeleteChildResourcesMembershipRequest request,
                                      StreamObserver<OperationStatusResponse> responseObserver) {
        try {

            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();
            GenericResource resource = request.getParentResource();
            List<GenericResource> childResources = request.getChildResourcesList();

            List<GenericResource> allResources = new ArrayList<>();
            allResources.add(resource);
            allResources.addAll(childResources);

            //TODO: can create raise conditions please move to DB level logic
            allResources.forEach(res -> {
                try {
                    if (!hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), res.getResourceId(), res.getType())) {
                        String msg = " Don't have access to change memberships";
                        responseObserver.onError(Status.PERMISSION_DENIED.withDescription(msg).asRuntimeException());
                        return;
                    }
                } catch (Exception exception) {
                    logger.error(" Error occurred while checking for permissions: Message {} "
                            + exception.getMessage(), exception);
                    String msg = " Error occurred while checking for permissions ";
                    responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
                    return;
                }
            });


            childResources.forEach(childResource -> {
                Map<String, Object> userProps = new HashMap<>();
                userProps.put("tenantId", callUser.getTenantId());
                userProps.put("entityId", resource.getResourceId());
                userProps.put("childEntityId", childResource.getResourceId());
                String query = "MATCH (cr:" + childResource.getType() + ")-[crel:CHILD_OF]->(r:" + resource.getType() + ")  where " +
                        " r.entityId = $entityId AND r.tenantId = $tenantId  AND cr.entityId = $childEntityId AND cr.tenantId = $tenantId " +
                        "delete crel";
                this.neo4JConnector.runTransactionalQuery(userProps, query);
            });

            responseObserver.onNext(OperationStatusResponse.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();


        } catch (Exception e) {
            String msg = " Error occurred while deleting  child memberships " + e.getMessage();
            logger.error(" Error occurred while fetching  parent resources: Messages {} ", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }


    }


    @Override
    public void fetchParentResources(ParentResourcesFetchRequest
                                             request, StreamObserver<ParentResourcesFetchResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();
            String resourseId = request.getResourceId();
            String type = request.getType();
            int depth = request.getDepth();
            if (depth == 0) {
                depth = 1;
            }
            if (type == null || type.isEmpty()) {
                type = "";
            } else {
                type = ":" + type;
            }

            if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), resourseId, type)) {
                Map<String, Object> userProps = new HashMap<>();
                userProps.put("tenantId", callUser.getTenantId());
                userProps.put("entityId", resourseId);
                String query = "MATCH  (r" + type + ")  where  r.entityId = $entityId AND r.tenantId = $tenantId" +
                        " MATCH (r)-[ch:CHILD_OF*1.." + depth + "]->(m) return distinct m";
                List<Record> records = this.neo4JConnector.searchNodes(userProps, query);
                if (!records.isEmpty()) {
                    List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
                    Map<String, GenericResource> genericResourceMap = new HashMap<>();
                    AtomicInteger count = new AtomicInteger();
                    genericResourceList.forEach(resource -> {
                        genericResourceMap.put(String.valueOf(count.get()), resource);
                        count.getAndIncrement();
                    });

                    ParentResourcesFetchResponse.Builder builder = ParentResourcesFetchResponse.newBuilder();
                    builder.putAllProperties(genericResourceMap);
                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();
                } else {
                    ParentResourcesFetchResponse.Builder builder = ParentResourcesFetchResponse.newBuilder();
                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();
                }
            } else {
                String msg = " Don't have access to change memberships";
                responseObserver.onError(Status.PERMISSION_DENIED.withDescription(msg).asRuntimeException());
                return;
            }
        } catch (Exception ex) {
            String msg = " Error occurred while fetching  parent resources " + ex.getMessage();
            logger.error(" Error occurred while fetching  parent resources: Messages {} ", ex.getMessage(), ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void addResourceMetadata(AddResourceMetadataRequest request, StreamObserver<Empty> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            String parentResourceId = request.getResourceId();
            String type = request.getType();

            Struct struct = request.getMetadata();
            String message = JsonFormat.printer().print(struct);
            JSONObject json = new JSONObject(message);

            Map<String, Object> map = json.toMap();

            mergeProperties(parentResourceId, type, callUser.getTenantId(), parentResourceId, map);

            Map<String, Object> parameters = new HashMap<>();
            Map<String, Object> properties = new HashMap<>();
            properties.put("metadata", message);
            parameters.put("props", properties);
            parameters.put("parentResourceId", parentResourceId);
            parameters.put("resourceId", UUID.randomUUID().toString());
            parameters.put("tenantId", callUser.getTenantId());

            String query = " MATCH (r:" + type + ") where r.entityId= $parentResourceId AND r.tenantId= $tenantId " +
                    " MERGE (cr:FULL_METADATA_NODE {entityId: $resourceId,tenantId: $tenantId})" +
                    " MERGE (r)-[:HAS_FULL_METADATA]->(cr) SET cr += $props  return cr";
            this.neo4JConnector.runTransactionalQuery(parameters, query);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = " Error occurred while adding resource metadata " + ex.getMessage();
            logger.error(" Error occurred while adding resource metadata: Messages {} ", ex.getMessage(), ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void fetchResourceMetadata(FetchResourceMetadataRequest
                                              request, StreamObserver<FetchResourceMetadataResponse> responseObserver) {
        try {
            AuthenticatedUser callUser = request.getAuthToken().getAuthenticatedUser();

            String resourceId = request.getResourceId();
            String type = request.getType();
            if (type == null || type.isEmpty()) {
                type = "";
            } else {
                type = ":" + type;
            }

            if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), resourceId, type)) {
                Optional<List<String>> metadataArray = readMetadata(resourceId, type, callUser.getTenantId());
                FetchResourceMetadataResponse.Builder builder = FetchResourceMetadataResponse.newBuilder();
                if (metadataArray.isPresent()) {
                    metadataArray.get().forEach(val -> {
                        try {
                            Struct.Builder structBuilder = Struct.newBuilder();
                            JsonFormat.parser().merge(val, structBuilder);
                            builder.addMetadata(structBuilder.build());
                        } catch (InvalidProtocolBufferException e) {
                            String msg = " Error occurred while fetching resource metadata " + e.getMessage();
                            logger.error(" Error occurred while fetching resource metadata: Messages {} ", e.getMessage(), e);
                            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
                            return;
                        }
                    });
                }
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
            } else {
                String msg = " Cannot find accessible resource ";
                logger.error(" Cannot find accessible resource");
                responseObserver.onError(Status.PERMISSION_DENIED.withDescription(msg).asRuntimeException());

            }
        } catch (Exception ex) {
            String msg = " Error occurred while fetching resource metadata " + ex.getMessage();
            logger.error(" Error occurred while fetching resource metadata: Messages {} ", ex.getMessage(), ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }

    }


    private boolean hasAccessForResource(String username, String tenantId, String resourceId, String type) throws
            Exception {
        Map<String, Object> userProps = new HashMap<>();
        userProps.put("username", username);
        userProps.put("tenantId", tenantId);
        userProps.put("entityId", resourceId);

        String query = " MATCH (u:User),  (r) where u.username = $username AND u.tenantId = $tenantId AND " +
                " r.entityId = $entityId AND r.tenantId = $tenantId" +
                " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g:Group)<-[:MEMBER_OF]-(u)" +
                " return case when  exists((u)<-[:SHARED_WITH]-(r)) OR  exists((g)<-[:SHARED_WITH]-(r)) OR   " +
                "exists((cg)<-[:SHARED_WITH]-(r)) then r  else NULL end as value";

        List<Record> records = this.neo4JConnector.searchNodes(userProps, query);

        List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
        if (genericResourceList.isEmpty()) {
            return false;
        }

        return true;
    }


    private void mergeProperties(String parentResourceId, String parentType, String tenantId, String resourceId,
                                 Map<String, Object> values) {
        for (String key : values.keySet()) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("parentResourceId", parentResourceId);
            parameters.put("tenantId", tenantId);
            if (parentResourceId.equals(resourceId) && !(values.get(key) instanceof Map) && !(values.get(key) instanceof List)) {
                Map<String, String> props = new HashMap<>();
                parameters.put("resourceId", resourceId);
                String value = String.valueOf(values.get(key));
                props.put(key, value);
                parameters.put("props", props);
                String query = " MATCH (r:" + parentType + ") where r.entityId= $parentResourceId " +
                        "AND r.tenantId= $tenantId" +
                        " SET r += $props  return r";
                this.neo4JConnector.runTransactionalQuery(parameters, query);

            } else if (values.get(key) instanceof Map) {
                String newResourceId = UUID.randomUUID().toString();
                parameters.put("resourceId", newResourceId);
                String type = "METADATA_NODE";
                Map<String, Object> hashMap = (Map<String, Object>) values.get(key);
                Map<String, Object> newHashMap = new HashMap<>();
                newHashMap.put(DATA_LAKE_JSON_IDENTIFIER, key);
                newHashMap.putAll(hashMap);
                String query = " MATCH (r:" + parentType + ") where r.entityId= $parentResourceId AND r.tenantId= $tenantId " +
                        " MERGE (cr:" + type + " {entityId: $resourceId,tenantId: $tenantId})" +
                        " MERGE (r)-[:HAS_METADATA]->(cr) return cr";
                this.neo4JConnector.runTransactionalQuery(parameters, query);

                mergeProperties(newResourceId, type, tenantId, newResourceId, newHashMap);

            } else if (values.get(key) instanceof List) {
                ArrayList arrayList = (ArrayList) values.get(key);
                Map<String, Object[]> props = new HashMap<>();
                parameters.put("resourceId", resourceId);
                props.put(key, arrayList.toArray());
                parameters.put("props", props);
                String query = " MATCH (r:" + parentType + ") where r.entityId= $parentResourceId " +
                        "AND r.tenantId= $tenantId" +
                        " SET r += $props  return r";
                this.neo4JConnector.runTransactionalQuery(parameters, query);
            }
        }
    }

    private Map<String, Object> readProperties(String resourceId, String type, String tenantId, Map<String, Object> map) throws Exception {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("entityId", resourceId);
        parameters.put("tenantId", tenantId);
        String query = " Match (r" + type + ") where r.entityId=$entityId and r.tenantId=$tenantId " +
                " Match (r)-[:HAS_METADATA*]->(m) return m";
        List<Record> records = this.neo4JConnector.searchNodes(parameters, query);

        List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
        if (!genericResourceList.isEmpty()) {
            genericResourceList.forEach(res -> {
                String resId = res.getResourceId();
                Map<String, String> propertiesMap = res.getPropertiesMap();
                String value = propertiesMap.get(DATA_LAKE_JSON_IDENTIFIER);
                ((Map<String, Object>) map.computeIfAbsent(value, val -> new HashMap<String, Object>())).putAll(propertiesMap);
                ((Map<String, Object>) map.get(value)).remove(DATA_LAKE_JSON_IDENTIFIER);
//                propertiesMap.forEach((key, val) -> {
//                    if (!key.equals(DATA_LAKE_JSON_IDENTIFIER)) {
//                        ((Map<String, Object>) map.get(value)).put(key, val);
//
//                    }
//                });

                try {
                    readProperties(resId, ":METADATA_NODE", tenantId, (Map<String, Object>) map.get(value));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

            });

        }
        return map;
    }


    private Optional<List<String>> readMetadata(String resourceId, String type, String tenantId) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("entityId", resourceId);
        parameters.put("tenantId", tenantId);
        String query = " Match (r" + type + ") where r.entityId=$entityId and r.tenantId=$tenantId " +
                " Match (r)-[:HAS_FULL_METADATA]->(m) return m";
        List<Record> records = this.neo4JConnector.searchNodes(parameters, query);

        List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
        if (!genericResourceList.isEmpty()) {
            return Optional.ofNullable(genericResourceList.stream().map(val -> {
                Map<String, String> proprties = val.getPropertiesMap();
                return proprties.get("metadata");
            }).collect(Collectors.toList()));
        }
        return Optional.empty();
    }

}
