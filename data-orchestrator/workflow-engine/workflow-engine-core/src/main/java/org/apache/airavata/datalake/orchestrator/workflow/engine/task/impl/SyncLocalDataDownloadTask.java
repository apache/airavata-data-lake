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

import org.apache.airavata.datalake.orchestrator.workflow.engine.task.BlockingTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.BlockingTaskDef;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.TaskParam;
import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.*;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.common.DelegateAuth;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

@BlockingTaskDef(name = "SyncLocalDataDownloadTask")
public class SyncLocalDataDownloadTask extends BlockingTask {

    private final static Logger logger = LoggerFactory.getLogger(SyncLocalDataDownloadTask.class);

    @TaskParam(name = "MFTAPIHost")
    private final ThreadLocal<String> mftHost = new ThreadLocal<>();

    @TaskParam(name = "MFTAPIPort")
    private final ThreadLocal<Integer> mftPort = new ThreadLocal<>();

    // Security
    @TaskParam(name = "UserId")
    private final ThreadLocal<String> userId = new ThreadLocal<>();

    @TaskParam(name = "MFTClientId")
    private final ThreadLocal<String> mftClientId = new ThreadLocal<>();

    @TaskParam(name = "MFTClientSecret")
    private final ThreadLocal<String> mftClientSecret = new ThreadLocal<>();

    @TaskParam(name = "TenantId")
    private final ThreadLocal<String> tenantId = new ThreadLocal<>();

    @TaskParam(name = "SourceResourceId")
    private final ThreadLocal<String> sourceResourceId = new ThreadLocal<>();

    @TaskParam(name = "SourceCredToken")
    private final ThreadLocal<String> sourceCredToken = new ThreadLocal<>();

    public static void main(String args[]) {


        SyncLocalDataDownloadTask task = new SyncLocalDataDownloadTask();
        task.setMftClientId("mft-agent");
        task.setMftClientSecret("kHqH27BloDCbLvwUA8ZYRlHcJxXZyby9PB90bTdU");
        task.setUserId("isjarana");
        task.setTenantId("custos-ii8g0cfwsz6ruwezykn9-10002640");
        task.setMftHost("149.165.157.235");
        task.setMftPort(7004);
        task.setSourceResourceId("46c7659360f8bdb1473fc16572d98a8611cfd7bb2296660dcd917ed4b6d33c13");
        task.setSourceCredToken("ed1af924-2a91-412c-bedf-d9321252456d");

        task.runBlockingCode();
    }

    @Override
    public TaskResult runBlockingCode() {


        DelegateAuth delegateAuth = DelegateAuth.newBuilder()
                .setUserId(getUserId())
                .setClientId(getMftClientId())
                .setClientSecret(getMftClientSecret())
                .putProperties("TENANT_ID", getTenantId()).build();

        HttpDownloadApiResponse httpDownloadApiResponse;
        FileMetadataResponse metadata;

        try (MFTApiClient mftClient = new MFTApiClient(getMftHost(), getMftPort())) {

            MFTApiServiceGrpc.MFTApiServiceBlockingStub mftClientStub = mftClient.get();

            httpDownloadApiResponse = mftClientStub.submitHttpDownload(HttpDownloadApiRequest
                    .newBuilder()
                    .setMftAuthorizationToken(AuthToken.newBuilder().setDelegateAuth(delegateAuth).build())
                    .setSourceResourceId(getSourceResourceId())
                    .setSourceToken(getSourceCredToken())
                    .setSourceType("SCP")
                    .setSourceResourceChildPath("")
                    .build());

            metadata = mftClientStub.getFileResourceMetadata(FetchResourceMetadataRequest.newBuilder()
                    .setResourceType("SCP")
                    .setResourceId(getSourceResourceId())
                    .setResourceToken(getSourceCredToken())
                    .setMftAuthorizationToken(AuthToken.newBuilder().setDelegateAuth(delegateAuth).build()).build());

        } catch (IOException e) {
            logger.error("Failed to create the mft client", e);
            return new TaskResult(TaskResult.Status.FAILED, "Failed to create the mft client");
        }

        String downloadUrl = httpDownloadApiResponse.getUrl();
        logger.info("Using download URL {}", downloadUrl);

        String downloadPath = "/tmp/" + metadata.getFriendlyName();
        try (BufferedInputStream in = new BufferedInputStream(new URL(downloadUrl).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(downloadPath)) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
        } catch (IOException e) {
            logger.error("Failed to download file", e);
            return new TaskResult(TaskResult.Status.FAILED, "Failed to download file");
        }

        logger.info("Downloaded to path {}", downloadPath);

        putUserContent("DOWNLOAD_PATH", downloadPath, Scope.WORKFLOW);
        return new TaskResult(TaskResult.Status.COMPLETED, "Success");
    }

    public String getSourceResourceId() {
        return sourceResourceId.get();
    }

    public void setSourceResourceId(String sourceResourceId) {
        this.sourceResourceId.set(sourceResourceId);
    }

    public String getSourceCredToken() {
        return sourceCredToken.get();
    }

    public void setSourceCredToken(String sourceCredToken) {
        this.sourceCredToken.set(sourceCredToken);
    }

    public String getUserId() {
        return userId.get();
    }

    public void setUserId(String userId) {
        this.userId.set(userId);
    }

    public String getMftHost() {
        return mftHost.get();
    }

    public void setMftHost(String mftHost) {
        this.mftHost.set(mftHost);
    }

    public Integer getMftPort() {
        return mftPort.get();
    }

    public void setMftPort(Integer mftPort) {
        this.mftPort.set(mftPort);
    }

    public String getMftClientId() {
        return mftClientId.get();
    }

    public void setMftClientId(String mftClientId) {
        this.mftClientId.set(mftClientId);
    }

    public String getMftClientSecret() {
        return mftClientSecret.get();
    }

    public void setMftClientSecret(String mftClientSecret) {
        this.mftClientSecret.set(mftClientSecret);
    }

    public String getTenantId() {
        return tenantId.get();
    }

    public void setTenantId(String tenantId) {
        this.tenantId.set(tenantId);
    }
}
