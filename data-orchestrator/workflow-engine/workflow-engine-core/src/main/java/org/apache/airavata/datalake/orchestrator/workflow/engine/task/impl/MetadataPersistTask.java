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

package org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl;

import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.drms.AuthCredentialType;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.storage.AddResourceMetadataRequest;
import org.apache.airavata.datalake.drms.storage.ResourceServiceGrpc;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.BlockingTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.BlockingTaskDef;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.TaskParam;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@BlockingTaskDef(name = "MetadataPersistTask")
public class MetadataPersistTask extends BlockingTask {

    private final static Logger logger = LoggerFactory.getLogger(MetadataPersistTask.class);

    @TaskParam(name = "JSON_FILE")
    private final ThreadLocal<String> jsonFile = new ThreadLocal<>();

    @TaskParam(name = "DRMS_HOST")
    private final ThreadLocal<String> drmsHost = new ThreadLocal<>();

    @TaskParam(name = "DRMS_PORT")
    private final ThreadLocal<Integer> drmsPort = new ThreadLocal<>();

    @TaskParam(name = "RESOURCE_ID")
    private final ThreadLocal<String> resourceId = new ThreadLocal<>();

    @TaskParam(name = "DRMS_SERVICE_ACCOUNT_KEY")
    private final ThreadLocal<String> serviceAccountKey = new ThreadLocal<>();

    @TaskParam(name = "DRMS_SERVICE_ACCOUNT_SECRET")
    private final ThreadLocal<String> serviceAccountSecret = new ThreadLocal<>();

    @TaskParam(name = "USER")
    private final ThreadLocal<String> user = new ThreadLocal<>();

    @TaskParam(name = "TENANT")
    private final ThreadLocal<String> tenant = new ThreadLocal<>();

    @Override
    public TaskResult runBlockingCode() throws Exception {

        String derivedFilePath = getJsonFile();
        if (derivedFilePath.startsWith("$")) {
            logger.info("Fetching json file path from cotext for key {}", derivedFilePath);
            derivedFilePath = getUserContent(derivedFilePath.substring(1), Scope.WORKFLOW);
        }

        logger.info("Using json file {}", derivedFilePath);

        String jsonString = new String(Files.readAllBytes(Path.of(derivedFilePath)));
        //logger.info("Json Content {}", jsonString);

        Struct.Builder structBuilder = Struct.newBuilder();
        JsonFormat.parser().merge(jsonString, structBuilder);

        logger.info("Adding metadata to resource {}", getResourceId());

        DRMSServiceAuthToken serviceAuthToken = DRMSServiceAuthToken.newBuilder()
                .setAccessToken(Base64.getEncoder()
                        .encodeToString((getServiceAccountKey() + ":" + getServiceAccountSecret()).getBytes(StandardCharsets.UTF_8)))
                .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                        .setUsername(getUser())
                        .setTenantId(getTenant())
                        .build())
                .build();

        ManagedChannel channel = null;
        try {

            channel = ManagedChannelBuilder.forAddress(getDrmsHost(), getDrmsPort()).usePlaintext().build();

            ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceGrpc.newBlockingStub(channel);

            resourceClient.addResourceMetadata(AddResourceMetadataRequest.newBuilder()
                    .setResourceId(getResourceId())
                    .setAuthToken(serviceAuthToken)
                    .setType("FILE")
                    .setMetadata(structBuilder.build()).build());

            logger.info("Successfully added metadata to resource {}", getResourceId());

            return new TaskResult(TaskResult.Status.COMPLETED, "Completed");
        } finally {
            if (channel != null) {
                channel.shutdown();
            }
        }
    }

    public String getJsonFile() {
        return jsonFile.get();
    }

    public void setJsonFile(String jsonFile) {
        this.jsonFile.set(jsonFile);
    }

    public String getDrmsHost() {
        return drmsHost.get();
    }

    public void setDrmsHost(String drmsHost) {
        this.drmsHost.set(drmsHost);
    }

    public int getDrmsPort() {
        return drmsPort.get();
    }

    public void setDrmsPort(int drmsPort) {
        this.drmsPort.set(drmsPort);
    }

    public String getResourceId() {
        return resourceId.get();
    }

    public void setResourceId(String resourceId) {
        this.resourceId.set(resourceId);
    }

    public String getServiceAccountKey() {
        return serviceAccountKey.get();
    }

    public void setServiceAccountKey(String serviceAccountKey) {
        this.serviceAccountKey.set(serviceAccountKey);
    }

    public String getServiceAccountSecret() {
        return serviceAccountSecret.get();
    }

    public void setServiceAccountSecret(String serviceAccountSecret) {
        this.serviceAccountSecret.set(serviceAccountSecret);
    }

    public String getUser() {
        return user.get();
    }

    public void setUser(String user) {
        this.user.set(user);
    }

    public String getTenant() {
        return tenant.get();
    }

    public void setTenant(String tenant) {
        this.tenant.set(tenant);
    }
}
