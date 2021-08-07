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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.data.orchestrator.api.stub.parsing.*;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.BlockingTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.BlockingTaskDef;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.TaskParam;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.types.StringMap;
import org.apache.commons.io.IOUtils;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@BlockingTaskDef(name = "GenericDataParsingTask")
public class GenericDataParsingTask extends BlockingTask {

    private final static Logger logger = LoggerFactory.getLogger(GenericDataParsingTask.class);

    @TaskParam(name = "ParserID")
    private ThreadLocal<String> parserId = new ThreadLocal<>();

    @TaskParam(name = "InputMapping")
    private ThreadLocal<StringMap> inputMapping = new ThreadLocal<>();

    @Override
    public TaskResult runBlockingCode() {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6566).usePlaintext().build();
        DataParserServiceGrpc.DataParserServiceBlockingStub parserClient = DataParserServiceGrpc.newBlockingStub(channel);
        ParserFetchResponse parserFetchResponse = parserClient
                .fetchParser(ParserFetchRequest.newBuilder()
                        .setParserId(getParserId()).build());

        DataParser parser = parserFetchResponse.getParser();
        List<DataParserInputInterface> inputInterfaces = parser.getInputInterfacesList();

        String tempWorkDir = "/tmp/" + UUID.randomUUID();
        String tempInputDir = tempWorkDir + File.separator + "inputs";
        String tempOutputDir = tempWorkDir + File.separator + "outputs";
        logger.info("Using temp working directory {}", tempWorkDir);
        try {
            Files.createDirectory(Paths.get(tempWorkDir));
            Files.createDirectory(Paths.get(tempInputDir));
            Files.createDirectory(Paths.get(tempOutputDir));
        } catch (IOException e) {
            logger.error("Failed to create temp working directories in {}", tempWorkDir, e);
            return new TaskResult(TaskResult.Status.FAILED, "Failed to create temp working directories");
        }

        for (DataParserInputInterface dpi: inputInterfaces) {
            String path = getInputMapping().get(dpi.getParserInputInterfaceId());
            if (path == null) {
                logger.error("No value specified for input {}", dpi.getParserInputInterfaceId());
                return new TaskResult(TaskResult.Status.FAILED, "No value specified for input");
            }

            if (path.startsWith("$")) {
                path = getUserContent(path.substring(1), Scope.WORKFLOW);
                if (path == null) {
                    logger.error("No value in context to path {} for {}", path, dpi.getParserInputInterfaceId());
                    return new TaskResult(TaskResult.Status.FAILED, "No value specified in context for path");
                }
            }

            try {
                Files.copy(Paths.get(path), Paths.get(tempInputDir + File.separator + dpi.getInputName()));
                logger.info("Copied input file from path {} to {}", path, tempInputDir + File.separator + dpi.getInputName());
            } catch (IOException e) {
                logger.error("Failed to copy the input from path {} to {}", path, tempInputDir);
                return new TaskResult(TaskResult.Status.FAILED, "Failed to copy the input");
            }
        }

        try {
            runContainer(parser, tempInputDir, tempOutputDir, new HashMap<>());
            exportOutputs(parser, tempOutputDir);
        } catch (Exception e) {
            logger.error("Failed to execute the container for task {}", getTaskId());
            return new TaskResult(TaskResult.Status.FAILED, "Failed to execute the container");
        }
        return new TaskResult(TaskResult.Status.COMPLETED, "Completed");
    }

    private void exportOutputs(DataParser parser, String outputPath) {
        for (DataParserOutputInterface dpoi: parser.getOutputInterfacesList()) {
            putUserContent(getTaskId() + "-" + dpoi.getOutputName(),
                    outputPath + File.separator + dpoi.getOutputName(),
                    Scope.WORKFLOW);
        }
    }

    private void runContainer(DataParser parser, String inputPath, String outputPath, Map<String, String> environmentValues)
            throws Exception{

        DefaultDockerClientConfig.Builder config = DefaultDockerClientConfig.createDefaultConfigBuilder();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config.build()).build();

        logger.info("Pulling image " + parser.getDockerImage());
        try {
            dockerClient.pullImageCmd(parser.getDockerImage().split(":")[0])
                    .withTag(parser.getDockerImage().split(":")[1])
                    .exec(new PullImageResultCallback()).awaitCompletion();
        } catch (InterruptedException e) {
            logger.error("Interrupted while pulling image", e);
            throw e;
        }

        logger.info("Successfully pulled image " + parser.getDockerImage());

        String containerId = UUID.randomUUID().toString();
        String commands[] = parser.getExecCommand().split(" ");
        CreateContainerResponse containerResponse = dockerClient.createContainerCmd(parser.getDockerImage()).withCmd(commands).withName(containerId)
                .withBinds(Bind.parse(inputPath + ":" + parser.getInputPath()),
                        Bind.parse(outputPath + ":" + parser.getOutputPath()))
                .withTty(true)
                .withAttachStdin(true)
                .withAttachStdout(true).withEnv(environmentValues.entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.toList()))
                .exec();

        logger.info("Created the container with id " + containerResponse.getId());

        final StringBuilder dockerLogs = new StringBuilder();

        if (containerResponse.getWarnings() != null && containerResponse.getWarnings().length > 0) {
            StringBuilder warningStr = new StringBuilder();
            for (String w : containerResponse.getWarnings()) {
                warningStr.append(w).append(",");
            }
            logger.warn("Container " + containerResponse.getId() + " warnings : " + warningStr);
        } else {
            logger.info("Starting container with id " + containerResponse.getId());
            dockerClient.startContainerCmd(containerResponse.getId()).exec();
            LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerResponse.getId()).withStdOut(true).withStdErr(true);

            try {

                logContainerCmd.exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame item) {
                        logger.info("Got frame: {}", item);;
                        if (item.getStreamType() == StreamType.STDOUT) {
                            dockerLogs.append(new String(item.getPayload(), StandardCharsets.UTF_8));
                            dockerLogs.append("\n");
                        } else if (item.getStreamType() == StreamType.STDERR) {
                            dockerLogs.append(new String(item.getPayload(), StandardCharsets.UTF_8));
                            dockerLogs.append("\n");
                        }
                        super.onNext(item);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logger.error("Errored while running the container {}", containerId, throwable);
                        super.onError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        logger.info("Container {} successfully completed", containerId);
                        super.onComplete();
                    }
                }).awaitCompletion();
            } catch (InterruptedException e) {
                logger.error("Interrupted while reading container log" + e.getMessage());
                throw e;
            }

            logger.info("Waiting for the container to stop");

            Integer statusCode = dockerClient.waitContainerCmd(containerResponse.getId()).exec(new WaitContainerResultCallback()).awaitStatusCode();
            logger.info("Container " + containerResponse.getId() + " exited with status code " + statusCode);
            if (statusCode != 0) {
                logger.error("Failing as non zero status code was returned");
                throw new Exception("Failing as non zero status code was returned");
            }

            logger.info("Container logs " + dockerLogs.toString());
        }
    }

    public String getParserId() {
        return parserId.get();
    }

    public void setParserId(String parserId) {
        this.parserId.set(parserId);
    }

    public StringMap getInputMapping() {
        return inputMapping.get();
    }

    public void setInputMapping(StringMap inputMapping) {
        this.inputMapping.set(inputMapping);
    }
}
