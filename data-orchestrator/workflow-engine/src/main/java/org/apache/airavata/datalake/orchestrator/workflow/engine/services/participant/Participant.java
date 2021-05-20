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

package org.apache.airavata.datalake.orchestrator.workflow.engine.services.participant;

import org.apache.airavata.datalake.orchestrator.workflow.engine.task.BlockingTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.BlockingTaskDef;
import org.apache.helix.InstanceType;
import org.apache.helix.examples.OnlineOfflineStateModelFactory;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZKHelixManager;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.apache.helix.model.BuiltInStateModelDefinitions;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.participant.StateMachineEngine;
import org.apache.helix.task.TaskFactory;
import org.apache.helix.task.TaskStateModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

@SpringBootApplication()
@ComponentScan("org.apache.airavata.datalake.orchestrator.workflow.engine.services.participant")
public class Participant implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(Participant.class);

    private int shutdownGracePeriod = 30000;
    private int shutdownGraceRetries = 2;

    @org.springframework.beans.factory.annotation.Value("${zookeeper.connection}")
    private String zkAddress;

    @org.springframework.beans.factory.annotation.Value("${cluster.name}")
    private String clusterName;

    @org.springframework.beans.factory.annotation.Value("${participant.name}")
    private String participantName;

    @org.springframework.beans.factory.annotation.Value("${task.list.file}")
    private String taskListFile;

    private ZKHelixManager zkHelixManager;

    private List<String> blockingTaskClasses = new ArrayList<>();
    private List<String> nonBlockingTaskClasses = new ArrayList<>();

    private final List<String> runningTasks = Collections.synchronizedList(new ArrayList<String>());

    @Override
    public void run(String... args) throws Exception {
        logger.info("Staring Participant .....");

        loadTasks();

        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(zkAddress, ZkClient.DEFAULT_SESSION_TIMEOUT,
                    ZkClient.DEFAULT_CONNECTION_TIMEOUT, new ZNRecordSerializer());
            ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(zkClient);

            List<String> nodesInCluster = zkHelixAdmin.getInstancesInCluster(clusterName);

            if (!nodesInCluster.contains(participantName)) {
                InstanceConfig instanceConfig = new InstanceConfig(participantName);
                instanceConfig.setHostName("localhost");
                instanceConfig.setInstanceEnabled(true);
                zkHelixAdmin.addInstance(clusterName, instanceConfig);
                logger.info("Participant: " + participantName + " has been added to cluster: " + clusterName);

            } else {
                zkHelixAdmin.enableInstance(clusterName, participantName, true);
                logger.debug("Participant: " + participantName + " has been re-enabled at the cluster: " + clusterName);
            }

            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {
                        logger.debug("Participant: " + participantName + " shutdown hook called");
                        try {
                            zkHelixAdmin.enableInstance(clusterName, participantName, false);
                        } catch (Exception e) {
                            logger.warn("Participant: " + participantName + " was not disabled normally", e);
                        }
                        disconnect();
                    })
            );

            connect();

        } catch (Exception ex) {
            logger.error("Error running Participant: " + participantName + ", reason: " + ex, ex);
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
    }

    private void loadTasks() throws Exception {

        try {
            Yaml yaml = new Yaml();
            File listFile = new File(taskListFile);

            InputStream stream;
            if (listFile.exists()) {
                logger.info("Loading task list file {} from absolute path", taskListFile);
                stream = new FileInputStream(taskListFile);
            } else {
                logger.info("Loading task list file {} from class path", taskListFile);
                stream = Participant.class.getClassLoader().getResourceAsStream(taskListFile);
            }

            Object load = yaml.load(stream);

            if (load == null) {
                throw new Exception("Did not load the configuration from file " + taskListFile);
            }

            if (load instanceof Map) {
                Map rootMap = (Map) load;
                if (rootMap.containsKey("tasks")) {
                    Object tasksObj = rootMap.get("tasks");
                    if (tasksObj instanceof Map) {
                        Map tasksMap = (Map) tasksObj;
                        if (tasksMap.containsKey("blocking")) {
                            Object blockingTaskObj = tasksMap.get("blocking");
                            if (blockingTaskObj instanceof List) {
                                blockingTaskClasses = (List<String>) blockingTaskObj;
                                blockingTaskClasses.forEach(taskClz -> {
                                    logger.info("Loading blocking task " + taskClz);
                                });
                            }
                        }

                        if (tasksMap.containsKey("nonBlocking")) {
                            Object nonBlockingTaskObj = tasksMap.get("nonBlocking");
                            if (nonBlockingTaskObj instanceof List) {
                                nonBlockingTaskClasses = (List<String>) nonBlockingTaskObj;
                                nonBlockingTaskClasses.forEach(taskClz -> {
                                    logger.info("Loading non blocking task " + taskClz);
                                });
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Failed to load task list from file {}", taskListFile, e);
            throw e;
        }
    }

    private Map<String, TaskFactory> getTaskFactory() throws Exception {

        Map<String, TaskFactory> taskMap = new HashMap<>();

        for (String className : blockingTaskClasses) {
            try {
                logger.info("Loading blocking task {}", className);
                Class<?> taskClz = Class.forName(className);
                Object taskObj = taskClz.getConstructor().newInstance();
                BlockingTask blockingTask = (BlockingTask) taskObj;
                TaskFactory taskFactory = context -> {
                    blockingTask.setCallbackContext(context);
                    return blockingTask;
                };
                BlockingTaskDef btDef = blockingTask.getClass().getAnnotation(BlockingTaskDef.class);
                taskMap.put(btDef.name(), taskFactory);

            } catch (ClassNotFoundException e) {
                logger.error("Couldn't find a class with name {}", className);
                throw e;
            }
        }
        return taskMap;
    }

    private void connect() {
        try {
            zkHelixManager = new ZKHelixManager(clusterName, participantName, InstanceType.PARTICIPANT, zkAddress);
            // register online-offline model
            StateMachineEngine machineEngine = zkHelixManager.getStateMachineEngine();
            OnlineOfflineStateModelFactory factory = new OnlineOfflineStateModelFactory(participantName);
            machineEngine.registerStateModelFactory(BuiltInStateModelDefinitions.OnlineOffline.name(), factory);

            // register task model
            machineEngine.registerStateModelFactory("Task", new TaskStateModelFactory(zkHelixManager, getTaskFactory()));

            logger.debug("Participant: " + participantName + ", registered state model factories.");

            zkHelixManager.connect();
            logger.info("Participant: " + participantName + ", has connected to cluster: " + clusterName);

            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            logger.error("Participant: " + participantName + ", is interrupted! reason: " + ex, ex);
        } catch (Exception ex) {
            logger.error("Error in connect() for Participant: " + participantName + ", reason: " + ex, ex);
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        logger.info("Shutting down participant. Currently available tasks " + runningTasks.size());
        if (zkHelixManager != null) {
            if (runningTasks.size() > 0) {
                for (int i = 0; i <= shutdownGraceRetries; i++) {
                    logger.info("Shutting down gracefully [RETRY " + i + "]");
                    try {
                        Thread.sleep(shutdownGracePeriod);
                    } catch (InterruptedException e) {
                        logger.warn("Waiting for running tasks failed [RETRY " + i + "]", e);
                    }
                    if (runningTasks.size() == 0) {
                        break;
                    }
                }
            }
            logger.info("Participant: " + participantName + ", has disconnected from cluster: " + clusterName);
            zkHelixManager.disconnect();
        }
    }

    public static void main(String args[]) throws Exception {
        SpringApplication.run(Participant.class);
    }
}
