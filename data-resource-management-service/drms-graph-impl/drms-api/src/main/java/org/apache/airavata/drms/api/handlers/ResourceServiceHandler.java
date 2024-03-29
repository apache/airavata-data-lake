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

import com.google.gson.Gson;
import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MapEntry;
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
import org.apache.airavata.drms.core.deserializer.AnyStorageDeserializer;
import org.apache.airavata.drms.core.deserializer.GenericResourceDeserializer;
import org.apache.airavata.drms.core.deserializer.TransferMappingDeserializer;
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


            String query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId with u  " +
                    " Match (r" + type + ") where r.entityId = $entityId AND r.tenantId = $tenantId with u, r" +
                    " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u) " +
                    " OPTIONAL MATCH (cg:Group)-[:CHILD_OF]->(g)" +
                    " OPTIONAL MATCH (r)-[:CHILD_OF*]->(x:COLLECTION)" +
                    " return case when  exists((u)<-[:SHARED_WITH]-(r)) OR  exists((g)<-[:SHARED_WITH]-(r)) OR   " +
                    "exists((cg)<-[:SHARED_WITH]-(r)) OR exists((u)<-[:SHARED_WITH]-(x)) OR exists((g)<-[:SHARED_WITH]-(x)) OR exists((cg)<-[:SHARED_WITH]-(x))" +
                    "then r  else NULL end as value";

            logger.debug("Fetch resource query {}", query);

            List<Record> records = this.neo4JConnector.searchNodes(userProps, query);
            try {
                List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
                ResourceFetchResponse.Builder builder = ResourceFetchResponse.newBuilder();
                if (!genericResourceList.isEmpty()) {
                    Optional<AnyStorage> anyStorage = findStorage(resourceId, type, callUser.getTenantId());
                    GenericResource resource = genericResourceList.get(0);
                    if (anyStorage.isPresent()) {
                        if (anyStorage.get().getStorageCase().equals(AnyStorage.StorageCase.SSH_STORAGE)) {
                            resource = resource.toBuilder().setSshStorage(anyStorage.get().getSshStorage()).build();
                        } else {
                            resource = resource.toBuilder().setS3Storage(anyStorage.get().getS3Storage()).build();
                        }
                    }
                    builder.setResource(resource);
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
            logger.error("Error occurred while fetching child resource {}", request.getResourceId(), ex);
            String msg = "Error occurred while fetching child resource with id" + request.getResourceId();
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
            if (entityId == null || entityId.isEmpty()) {
                entityId = Utils.getId(request.getResource().toString());
            }
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
                serializedMap.put("firstName", callUser.getFirstName());
                serializedMap.put("lastName", callUser.getLastName());

                if (serializedMap.containsKey("properties") && serializedMap.get("properties") instanceof List) {
                    List propertiesList = (List) serializedMap.get("properties");
                    propertiesList.forEach(property -> {
                        MapEntry entry = (MapEntry) property;
                        serializedMap.put(entry.getKey().toString(), entry.getValue());
                    });
                }
                serializedMap.remove("properties");
                if (!parentId.isEmpty()) {
                    String parentLabel = request.getResource().getPropertiesMap().get("PARENT_TYPE");
                    if (parentLabel == null || parentLabel.isEmpty()) {
                        parentLabel = request.getResource().getPropertiesMap().get("parentType");
                    }
                    this.neo4JConnector.mergeNodesWithParentChildRelationShip(serializedMap, new HashMap<>(),
                            request.getResource().getType(), parentLabel, callUser.getUsername(), entityId,
                            parentId, callUser.getTenantId());
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

            String query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId with u  " +
                    " Match (r:" + type + ") where r.entityId = $entityId AND r.tenantId = $tenantId with u, r" +
                    " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u) " +
                    " OPTIONAL MATCH (cg:Group)-[:CHILD_OF]->(g)" +
                    " return case when  exists((u)<-[:SHARED_WITH]-(r)) OR  exists((g)<-[:SHARED_WITH]-(r)) OR   " +
                    "exists((cg)<-[:SHARED_WITH]-(r)) then r  else NULL end as value";

            logger.debug("Create resource query {}", query);

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
            logger.error("Error occurred while creating resource {}", request.getResource().getResourceId(), ex);
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

//            String query = " MATCH (u:User),  (r" + type + ") where u.username = $username AND u.tenantId = $tenantId AND " +
//                    " r.entityId = $entityId AND r.tenantId = $tenantId" +
//                    " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u)" +
//                    " OPTIONAL MATCH (u)<-[crRel:SHARED_WITH]-(r)<-[:CHILD_OF*]-(cr)" +
//                    " OPTIONAL MATCH (g)<-[chgrRel:SHARED_WITH]-(r)<-[:CHILD_OF*]-(chgr)" +
//                    " OPTIONAL MATCH (u)<-[prRelU:SHARED_WITH]-(pr:COLLECTION)<-[:CHILD_OF*]-(r)<-[:CHILD_OF]-(x)" +
//                    " OPTIONAL MATCH (g)<-[prRelG:SHARED_WITH]-(prg:COLLECTION)<-[:CHILD_OF*]-(r)<-[:CHILD_OF]-(y)" +
//                    " return distinct  cr,crRel, chgr,chgrRel, x, prRelU,y,prRelG";
//
//            if (depth == 1) {
            String query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId with u  " +
                    " Match (r" + type + ") where r.entityId = $entityId AND r.tenantId = $tenantId with u, r" +
                    " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u)" +
                    " OPTIONAL MATCH (u)<-[crRel:SHARED_WITH]-(r)<-[:CHILD_OF]-(cr)" +
                    " OPTIONAL MATCH (g)<-[chgrRel:SHARED_WITH]-(r)<-[:CHILD_OF]-(chgr)" +
                    " OPTIONAL MATCH (u)<-[prRelU:SHARED_WITH]-(pr:COLLECTION)<-[:CHILD_OF*]-(r)<-[:CHILD_OF]-(x)" +
                    " OPTIONAL MATCH (g)<-[prRelG:SHARED_WITH]-(prg:COLLECTION)<-[:CHILD_OF*]-(r)<-[:CHILD_OF]-(y)" +
                    " return distinct  cr,crRel, chgr,chgrRel, x, prRelU,y,prRelG";
//            }

            logger.debug("Fetch child query {}", query);


            List<Record> records = this.neo4JConnector.searchNodes(userProps, query);
            List keyList = new ArrayList();
            keyList.add("cr:crRel");
            keyList.add("chgr:chgrRel");
            keyList.add("chcgr:chcgrRel");
            keyList.add("x:prRelU");
            keyList.add("y:prRelG");
            List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records, keyList);
            ChildResourceFetchResponse.Builder builder = ChildResourceFetchResponse.newBuilder();
            builder.addAllResources(genericResourceList);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();


        } catch (Exception ex) {
            logger.error("Error occurred while fetching child resource {}", request.getResourceId(), ex);
            responseObserver.onError(Status.INTERNAL.withDescription("Error occurred while fetching child resource"
                    + ex.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void updateResource(ResourceUpdateRequest
                                       request, StreamObserver<ResourceUpdateResponse> responseObserver) {
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
                serializedMap.putAll(request.getResource().getPropertiesMap());
                if (serializedMap.containsKey("properties") && serializedMap.get("properties") instanceof List) {
                    List propertiesList = (List) serializedMap.get("properties");
                    propertiesList.forEach(property -> {
                        MapEntry entry = (MapEntry) property;
                        serializedMap.put(entry.getKey().toString(), entry.getValue());
                    });
                }
                serializedMap.remove("properties");

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

            String query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId with u  " +
                    " Match (r:" + type + ") where r.entityId = $entityId AND r.tenantId = $tenantId with u, r" +
                    " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u) " +
                    " OPTIONAL MATCH (cg:Group)-[:CHILD_OF]->(g)" +
                    " return case when  exists((u)<-[:SHARED_WITH]-(r)) OR  exists((g)<-[:SHARED_WITH]-(r)) OR   " +
                    "exists((cg)<-[:SHARED_WITH]-(r)) then r  else NULL end as value";


            logger.debug("Update query {}", query);

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
            ResourceUpdateResponse response = ResourceUpdateResponse
                    .newBuilder()
                    .setResource(genericResource)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();


        } catch (Exception ex) {
            logger.error("Error occurred while creating resource {}", request.getResource().getResourceId(), ex);
            String msg = "Error occurred while creating resource" + ex.getMessage();
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
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
                logger.error("Resource type required to continue search ");
                responseObserver
                        .onError(Status.FAILED_PRECONDITION.withDescription("Resource type required to continue search")
                                .asRuntimeException());
                return;
            }
            List<GenericResource> allowedResourceList = new ArrayList<>();
            List<String> storageList = new ArrayList<>();
            Optional<List<String>> globalStorages = getGlobalSourceStorage(callUser.getTenantId());
            if (globalStorages.isPresent()) {
                globalStorages.get().forEach(str -> {
                    storageList.add(str);
                });
            }

            List keyList = new ArrayList();

            keyList = new ArrayList();
            Map<String, Map<String, String>> searchParameterMap = new HashMap<>();
            boolean propertySearchEnabled = false;
            for (ResourceSearchQuery qry : resourceSearchQueries) {
                if (qry.getField().equals("storageId")) {
                    storageList.clear();
                    storageList.add(qry.getValue());
                } else if (qry.getField().equals("sharedBy")) {
                    searchParameterMap.computeIfAbsent("sharedBy", map -> new HashMap<>()).put("username", qry.getValue());
                    propertySearchEnabled = true;
                } else if (qry.getField().equals("sharedWith")) {
                    searchParameterMap.computeIfAbsent("sharedWith", map -> new HashMap<>()).put("username", qry.getValue());
                    propertySearchEnabled = true;
                } else {
                    searchParameterMap.computeIfAbsent("searchParams", map -> new HashMap<>()).put(qry.getField(), qry.getValue());
                    propertySearchEnabled = true;
                }
            }

            if (propertySearchEnabled) {
                if (searchParameterMap.containsKey("sharedBy") &&
                        (!searchParameterMap.containsKey("searchParams") || searchParameterMap.get("searchParams").isEmpty())) {
                    String val = searchParameterMap.get("sharedBy").get("username");
                    String query = " Match (m:" + value + ")-[r:SHARED_WITH]->(l) " +
                            "where r.sharedBy=$sharedBy AND m.tenantId=$tenantId AND  l.tenantId=$tenantId  AND NOT l.username=$sharedBy " +
                            "return m, r ";
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("sharedBy", val);
                    objectMap.put("tenantId", callUser.getTenantId());
                    List<Record> records = this.neo4JConnector.searchNodes(objectMap, query);
                    keyList.add("m:r");
                    List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records, keyList);
                    ResourceSearchResponse.Builder builder = ResourceSearchResponse.newBuilder();
                    builder.addAllResources(genericResourceList);
                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();
                    return;
                } else if (searchParameterMap.containsKey("sharedWith") &&
                        (!searchParameterMap.containsKey("searchParams") || searchParameterMap.get("searchParams").isEmpty())) {
                    String val = searchParameterMap.get("sharedWith").get("username");
                    String query = "MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId " +
                            " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u)  " +
                            " OPTIONAL MATCH (u)<-[pRel:SHARED_WITH]-(p:COLLECTION)" +
                            " where NOT  p.owner  = '" + val + "'  " +
                            " OPTIONAL MATCH (g)<-[pxRel:SHARED_WITH]-(pr:COLLECTION)" +
                            " where NOT  pr.owner  = '" + val + "'" +
                            " return distinct  p,pRel, pr,pxRel";
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("username", val);
                    objectMap.put("tenantId", callUser.getTenantId());
                    List<Record> records = this.neo4JConnector.searchNodes(objectMap, query);
                    keyList.add("p:pRel");
                    keyList.add("px:pxRel");
                    List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records, keyList);
                    ResourceSearchResponse.Builder builder = ResourceSearchResponse.newBuilder();
                    builder.addAllResources(genericResourceList);
                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();
                    return;
                }

                //TODO: replace with proper neo4j query


                if (searchParameterMap.containsKey("sharedBy") && (searchParameterMap.containsKey("searchParams")
                        && !searchParameterMap.get("searchParams").isEmpty())) {
                    String username = searchParameterMap.get("sharedBy").get("username");
                    searchParameterMap.get("searchParams").forEach((key, val) -> {
                        try {
                            for (String strId : storageList) {
                                List<GenericResource> genericResourceList = Utils
                                        .getMetadataSearchQueryForSharedByMe(value, key, val, strId, username, callUser.getTenantId(), neo4JConnector);
                                genericResourceList.forEach(res -> {
                                    try {
                                        if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), res.getResourceId(), "COLLECTION")) {
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

                            List<GenericResource> genericResources = Utils
                                    .getPropertySearchQueryForSharedByMe(value, key, val, username, callUser.getTenantId(), neo4JConnector);
                            genericResources.forEach(res -> {
                                try {
                                    if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), res.getResourceId(), "COLLECTION")) {
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

                        } catch (Exception exception) {
                            logger.error("Errored while searching generic resources");
                            responseObserver
                                    .onError(Status.INTERNAL.withDescription("Errored while searching generic resources ")
                                            .asRuntimeException());
                            return;
                        }
                    });
                } else if (searchParameterMap.containsKey("sharedWith") && (searchParameterMap.containsKey("searchParams")
                        && !searchParameterMap.get("searchParams").isEmpty())) {
                    String username = searchParameterMap.get("sharedWith").get("username");
                    searchParameterMap.get("searchParams").forEach((key, val) -> {
                        try {
                            for (String strId : storageList) {
                                List<GenericResource> genericResourceList = Utils
                                        .getMetadataSearchQueryForSharedWithMe(value, key, val, strId, username, callUser.getTenantId(), neo4JConnector);
                                genericResourceList.forEach(res -> {
                                    try {
                                        if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), res.getResourceId(), "COLLECTION")) {
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

                            List<GenericResource> genericResources = Utils
                                    .getPropertySearchQueryForSharedWithMe(value, key, val, username, callUser.getTenantId(), neo4JConnector);
                            genericResources.forEach(res -> {
                                try {
                                    if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), res.getResourceId(), "COLLECTION")) {
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
                        } catch (Exception exception) {
                            logger.error("Errored while searching generic resources");
                            responseObserver
                                    .onError(Status.INTERNAL.withDescription("Errored while searching generic resources ")
                                            .asRuntimeException());
                            return;
                        }
                    });

                } else {
                    for (String strId : storageList) {
                        Optional<String> metadataSearchQueryOP = Utils.getMetadataSearchQuery(resourceSearchQueries, value, strId);
                        Optional<String> ownPropertySearchQuery = Utils.getPropertySearchQuery(resourceSearchQueries, value, strId);
                        if (metadataSearchQueryOP.isPresent()) {
                            String query = metadataSearchQueryOP.get();

                            List<Record> records = this.neo4JConnector.searchNodes(query);
                            List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);


                            genericResourceList.forEach(res -> {
                                try {
                                    if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), res.getResourceId(), "COLLECTION")) {
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
                                        if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), res.getResourceId(), "COLLECTION")) {
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
                        }

                    }
                }
                ResourceSearchResponse.Builder builder = ResourceSearchResponse.newBuilder();
                builder.addAllResources(allowedResourceList);
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
                return;

            } else {
                String query = "";
                Map<String, Object> userProps = new HashMap<>();
                userProps.put("username", callUser.getUsername());
                userProps.put("tenantId", callUser.getTenantId());
                if ((value.equals("FILE") || value.equals("COLLECTION"))) {
                    for (String storageId : storageList) {

                        query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId with u" +
                                " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u) " +
                                " OPTIONAL MATCH (u)<-[relRM:SHARED_WITH]-(m)<-[:CHILD_OF*]-(rm:" + value + ")-[:CHILD_OF*]->(s:Storage{entityId:'" + storageId + "'})" +
                                " , (s:Storage{entityId:'" + storageId + "'})<-[:CHILD_OF*]-(r:" + value + ")-[relR:SHARED_WITH]->(u)" +
                                " OPTIONAL MATCH (g)<-[relRMG:SHARED_WITH]-(mg)<-[:CHILD_OF*]-(rmg:" + value + ")-[:CHILD_OF*]->(s:Storage{entityId:'" + storageId + "'})" +
                                " , (s:Storage{entityId:'" + storageId + "'})<-[:CHILD_OF*]-(rg:" + value + ")-[relRG:SHARED_WITH]->(g)" +
                                " return distinct  rm,relRM, r,relR, rmg,relRMG, rg,relRG ";
                        keyList = new ArrayList();
                        keyList.add("rm:relRM");
                        keyList.add("r:relR");
                        keyList.add("rmg:relRMG");
                        keyList.add("rg:relRG");
                        if (depth == 1) {
                            query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId with u" +
                                    " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u) " +
                                    " OPTIONAL MATCH (s:Storage{entityId:'" + storageId + "'})<-[:CHILD_OF]-(r:" + value + ")-[relR:SHARED_WITH]->(u)" +
                                    " OPTIONAL MATCH (sp:Storage{entityId:'" + storageId + "'})<-[:CHILD_OF]-(rg:" + value + ")-[relRG:SHARED_WITH]->(g)" +
                                    " OPTIONAL MATCH (s2:Storage{entityId:'" + storageId + "'})<-[:CHILD_OF*]-(r2:" + value + ")-[relR2:SHARED_WITH]->(u) where NOT r2.owner=$username " +
                                    " AND NOT (r2)-[:CHILD_OF*]->(r)" +
                                    " return distinct   r,relR, rg,relRG, r2,relR2";
                            keyList = new ArrayList();
                            keyList.add("r:relR");
                            keyList.add("rg:relRG");
                            keyList.add("r2:relR2");
                            keyList.add("r3:relR3");
                        }
                        logger.debug("Search query {}", query);

                        List<Record> records = this.neo4JConnector.searchNodes(userProps, query);

                        List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records, keyList);
                        allowedResourceList.addAll(genericResourceList);
                    }
                } else {
                    query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId with u" +
                            " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u) " +
                            " OPTIONAL MATCH (u)<-[relRM:SHARED_WITH]-(m)<-[:CHILD_OF*]-(rm:" + value + ")" +
                            " , (r:" + value + ")-[relR:SHARED_WITH]->(u)" +
                            " OPTIONAL MATCH (g)<-[relRMG:SHARED_WITH]-(mg)<-[:CHILD_OF*]-(rmg:" + value + ")" +
                            " , (rg:" + value + ")-[relRG:SHARED_WITH]->(g)" +
                            " return distinct  rm,relRM, r,relR, rmg,relRMG, rg, relRG ";
                    keyList = new ArrayList();
                    keyList.add("rm:relRM");
                    keyList.add("r:relR");
                    keyList.add("rmg:relRMG");
                    keyList.add("rg:relRG");
                    if (depth == 1) {
                        query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId with u" +
                                " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u) " +
                                " OPTIONAL MATCH (r:" + value + ")-[relR:SHARED_WITH]->(u)" +
                                " OPTIONAL MATCH (rg:" + value + ")-[relRG:SHARED_WITH]->(g)" +
                                " return distinct   r,relR, rg, relRG ";
                        keyList = new ArrayList();
                        keyList.add("r:relR");
                        keyList.add("rg:relRG");
                    }
                    List<Record> records = this.neo4JConnector.searchNodes(userProps, query);

                    List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records, keyList);
                    allowedResourceList.addAll(genericResourceList);
                }


            }
            ResourceSearchResponse.Builder builder = ResourceSearchResponse.newBuilder();
            builder.addAllResources(allowedResourceList);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();

        } catch (
                Exception e) {
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
                    if (!hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), res.getResourceId())) {
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
                    if (!hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), res.getResourceId())) {
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

            if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), resourseId)) {
                Map<String, Object> userProps = new HashMap<>();
                userProps.put("tenantId", callUser.getTenantId());
                userProps.put("entityId", resourseId);
                String query = "MATCH  (r" + type + ")  where  r.entityId = $entityId AND r.tenantId = $tenantId  with r" +
                        " MATCH (r)-[ch:CHILD_OF*1.." + depth + "]->(m) return distinct m";
                List<Record> records = this.neo4JConnector.searchNodes(userProps, query);
                if (!records.isEmpty()) {
                    List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
                    Map<String, GenericResource> genericResourceMap = new HashMap<>();
                    AtomicInteger count = new AtomicInteger();
                    genericResourceList.forEach(resource -> {
                        try {
                            if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), resource.getResourceId(), "COLLECTION")) {
                                genericResourceMap.put(String.valueOf(count.get()), resource);
                                count.getAndIncrement();
                            }
                        } catch (Exception exception) {
                            logger.error(" Error occurred while fetching  parent resources: {}", resource.getResourceId());
                        }
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
                String msg = " Don't have access to fetch resource " + resourseId;
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

//            parameters.put("props", properties);
            parameters.put("parentResourceId", parentResourceId);
            parameters.put("resourceId", UUID.randomUUID().toString());
            parameters.put("tenantId", callUser.getTenantId());

            if (type == null || type.isEmpty()) {
                type = "";
            } else {
                type = ":" + type;
            }
            Optional<List<String>> jsonList = readMetadata(parentResourceId, type, callUser.getTenantId());

            if (jsonList.isPresent() && !jsonList.get().isEmpty()) {

                String oldJSON = jsonList.get().get(0);
                message = mergeJSON(oldJSON, message);
            }
            parameters.put("metadata", message);
            String query = " MATCH (r" + type + ") where r.entityId= $parentResourceId AND r.tenantId= $tenantId with r" +
                    " MERGE (r)-[:HAS_FULL_METADATA]->(cr:FULL_METADATA_NODE{tenantId: $tenantId}) ON CREATE SET cr.metadata= $metadata " +
                    " ON MATCH SET cr.metadata = $metadata";
            this.neo4JConnector.runTransactionalQuery(parameters, query);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = " Error occurred while adding resource metadata " + ex.getMessage();
            // Issue https://github.com/neo4j/neo4j-java-driver/issues/773
            logger.error("Error occurred while adding resource metadata: Messages {}", ex.getMessage());
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

            if (hasAccessForResource(callUser.getUsername(), callUser.getTenantId(), resourceId)) {
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


    private boolean hasAccessForResource(String username, String tenantId, String resourceId) throws
            Exception {
        Map<String, Object> userProps = new HashMap<>();
        userProps.put("username", username);
        userProps.put("tenantId", tenantId);
        userProps.put("entityId", resourceId);

        String query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId with u  " +
                " Match (r) where r.entityId = $entityId AND r.tenantId = $tenantId with u, r" +
                " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u)" +
                " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g)" +
                " OPTIONAL MATCH (l)<-[:CHILD_OF*]-(r)" +
                " return case when  exists((u)<-[:SHARED_WITH]-(r)) OR exists((u)<-[:SHARED_WITH]-(l)) OR  exists((g)<-[:SHARED_WITH]-(r)) OR   " +
                " exists((g)<-[:SHARED_WITH]-(l)) OR exists((cg)<-[:SHARED_WITH]-(r)) OR  exists((cg)<-[:SHARED_WITH]-(l)) then r  else NULL end as value";

        List<Record> records = this.neo4JConnector.searchNodes(userProps, query);

        List<GenericResource> genericResourceList = GenericResourceDeserializer.deserializeList(records);
        if (genericResourceList.isEmpty()) {
            return false;
        }

        return true;
    }

    private boolean hasAccessForResource(String username, String tenantId, String resourceId, String parentType) throws
            Exception {
        Map<String, Object> userProps = new HashMap<>();
        userProps.put("username", username);
        userProps.put("tenantId", tenantId);
        userProps.put("entityId", resourceId);

        String query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId with u  " +
                " Match (r) where r.entityId = $entityId AND r.tenantId = $tenantId with u, r" +
                " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u)" +
                " OPTIONAL MATCH (cg:Group)-[:CHILD_OF*]->(g)" +
                " OPTIONAL MATCH (l:" + parentType + ")<-[:CHILD_OF*]-(r)" +
                " return case when  exists((u)<-[:SHARED_WITH]-(r)) OR exists((u)<-[:SHARED_WITH]-(l)) OR  exists((g)<-[:SHARED_WITH]-(r)) OR   " +
                " exists((g)<-[:SHARED_WITH]-(l)) OR exists((cg)<-[:SHARED_WITH]-(r)) OR  exists((cg)<-[:SHARED_WITH]-(l)) then r  else NULL end as value";

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


    private Optional<AnyStorage> findStorage(String entityId, String type, String tenantId) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("entityId", entityId);
        parameters.put("tenantId", tenantId);
        String query = " Match (r" + type + ") where r.entityId=$entityId and r.tenantId=$tenantId " +
                " Match (s:Storage)<-[:CHILD_OF*]-(r) return (s)";

        List<Record> records = this.neo4JConnector.searchNodes(parameters, query);
        if (!records.isEmpty()) {
            List<AnyStorage> storageList = AnyStorageDeserializer.deserializeList(records);
            return Optional.ofNullable(storageList.get(0));

        }
        return Optional.empty();
    }

    private Optional<List<String>> getGlobalSourceStorage(String tenantId) throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("tenantId", tenantId);
        properties.put("scope", TransferScope.GLOBAL.name());
        String query = " Match (srcStr:Storage)-[:TRANSFER_OUT]->(tm:TransferMapping) where tm.tenantId=$tenantId AND tm.scope='GLOBAL' " +
                " AND srcStr.tenantId=$tenantId return srcStr, tm";
        List<Record> records = this.neo4JConnector.searchNodes(properties, query);
        List<TransferMapping> sourceTransfers = TransferMappingDeserializer.deserializeListExceptDestinationStorage(records);
        List<String> arrayList = new ArrayList<>();
        if (sourceTransfers.isEmpty()) {
            return Optional.empty();
        }
        sourceTransfers.forEach(transfer -> {
            if (!arrayList.contains(getStorageId(transfer.getSourceStorage()))) {
                arrayList.add(getStorageId(transfer.getSourceStorage()));
            }

        });
        return Optional.ofNullable(arrayList);
    }

    private String getStorageId(AnyStorage storage) {
        if (storage.getStorageCase()
                .equals(AnyStorage.StorageCase.S3_STORAGE)) {
            return storage.getS3Storage().getStorageId();
        } else {
            return storage.getSshStorage().getStorageId();
        }
    }

    private String mergeJSON(String oldJSON, String newJSON) {
        Gson gson = new Gson();
//read both jsons
        Map<String, Object> json1 = gson.fromJson(oldJSON, Map.class);
        Map<String, Object> json2 = gson.fromJson(newJSON, Map.class);
//create combined json with contents of first json
        Map<String, Object> combined = new HashMap<>(json1);
//Add the contents of first json. It will overwrite the values of keys are
//same. e.g. "foo" of json2 will take precedence if both json1 and json2 have "foo"
        combined.putAll(json2);
        return gson.toJson(combined);
    }


}
