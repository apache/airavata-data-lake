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

package org.apache.airavata.datalake.orchestrator.workflow.engine.services.controller;

import org.apache.helix.controller.HelixControllerMain;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication()
@ComponentScan("org.apache.airavata.datalake.orchestrator.workflow.engine.services.controller")
public class Controller implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    @org.springframework.beans.factory.annotation.Value("${cluster.name}")
    private String clusterName;

    @org.springframework.beans.factory.annotation.Value("${controller.name}")
    private String controllerName;

    @org.springframework.beans.factory.annotation.Value("${zookeeper.connection}")
    private String zkAddress;

    private org.apache.helix.HelixManager zkHelixManager;

    private CountDownLatch startLatch = new CountDownLatch(1);
    private CountDownLatch stopLatch = new CountDownLatch(1);

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Cluster Controller ......");

        try {
            ZkClient zkClient = new ZkClient(zkAddress, ZkClient.DEFAULT_SESSION_TIMEOUT,
                    ZkClient.DEFAULT_CONNECTION_TIMEOUT, new ZNRecordSerializer());
            ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(zkClient);

            // Creates the zk cluster if not available
            if (!zkHelixAdmin.getClusters().contains(clusterName)) {
                zkHelixAdmin.addCluster(clusterName, true);
            }

            zkHelixAdmin.close();
            zkClient.close();

            logger.info("Connection to helix cluster : " + clusterName + " with name : " + controllerName);
            logger.info("Zookeeper connection string " + zkAddress);

            zkHelixManager = HelixControllerMain.startHelixController(zkAddress, clusterName,
                    controllerName, HelixControllerMain.STANDALONE);
            startLatch.countDown();
            stopLatch.await();
        } catch (Exception ex) {
            logger.error("Error in running the Controller: {}", controllerName, ex);
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        if (zkHelixManager != null) {
            logger.info("Controller: {}, has disconnected from cluster: {}", controllerName, clusterName);
            zkHelixManager.disconnect();
        }
    }

    public static void main(String args[]) throws Exception {
        SpringApplication.run(Controller.class);
    }
}
