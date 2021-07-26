/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.datalake.orchestrator;

import org.apache.airavata.datalake.orchestrator.handlers.async.OrchestratorEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * TODO: Spring Boot API
 */
@ComponentScan(basePackages = {"org.apache.airavata.datalake.orchestrator"})
@SpringBootApplication()
@EnableJpaAuditing
@EnableJpaRepositories("org.apache.airavata.datalake")
@EntityScan("org.apache.airavata.datalake")
public class APIServerInitializer implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(APIServerInitializer.class);

    @Autowired
    private OrchestratorEventHandler orchestratorEventHandler;

    @org.springframework.beans.factory.annotation.Value("${config.path}")
    private String configPath;

    public static void main(String[] args) {
        SpringApplication.run(APIServerInitializer.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Starting Data orchestrator API Server ...");

        LOGGER.info("Loading configuration from file {} ...", configPath);
        Configuration configuration = this.loadConfig(configPath);

        LOGGER.info("Registering Orchestration even handler " + OrchestratorEventHandler.class.getName() + " ...");
        orchestratorEventHandler.init(configuration);
        LOGGER.info("Data orchestrator start accepting  events ....");
        orchestratorEventHandler.startProcessing();
    }


    private Configuration loadConfig(String filePath) {
        LOGGER.info("File path " + filePath);
        try (InputStream in = new FileInputStream(filePath)) {
            Yaml yaml = new Yaml();
            return yaml.loadAs(in, Configuration.class);
        } catch (Exception exception) {
            LOGGER.error("Error loading config file", exception);
        }
        return null;
    }

}
