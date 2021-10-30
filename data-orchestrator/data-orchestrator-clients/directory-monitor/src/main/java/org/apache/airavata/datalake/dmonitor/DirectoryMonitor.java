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

package org.apache.airavata.datalake.dmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication()
public class DirectoryMonitor implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(DirectoryMonitor.class);

    @org.springframework.beans.factory.annotation.Value("${tenant.id}")
    private String tenantId;

    @org.springframework.beans.factory.annotation.Value("${base.path}")
    private String basePath;

    @org.springframework.beans.factory.annotation.Value("${monitor.depth}")
    private int monitorDepth;

    @org.springframework.beans.factory.annotation.Value("${resource.type}")
    private String resourceType;

    @org.springframework.beans.factory.annotation.Value("${host.name}")
    private String hostName;

    @org.springframework.beans.factory.annotation.Value("${event.type}")
    private String eventType;

    @org.springframework.beans.factory.annotation.Value("${auth.token}")
    private String authToken;

    @org.springframework.beans.factory.annotation.Value("${kafka.url}")
    private String kafkaUrl;

    @org.springframework.beans.factory.annotation.Value("${kafka.publisher.id}")
    private String kafkaPublisherId;

    @org.springframework.beans.factory.annotation.Value("${kafka.event.topic}")
    private String kafkaEventTopic;

    private Map<WatchKey, Path> watchKeyPathMap = new HashMap<>();

    public static void main(String args[]) {
        SpringApplication app = new SpringApplication(DirectoryMonitor.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    private void registerPathsRecursively(Path currentPath, int depth, WatchService watchService) throws IOException {
        File[] children = currentPath.toAbsolutePath().toFile().listFiles();

        if (depth < monitorDepth) {
            for (File child : children) {
                if (child.isDirectory()) {
                    Path childPath = Paths.get(child.getAbsolutePath());
                    WatchKey watchKey = childPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                    logger.info("Registering path {}", childPath.toAbsolutePath().toString());
                    watchKeyPathMap.put(watchKey, childPath);
                    registerPathsRecursively(childPath, depth + 1, watchService);
                }
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {

        EventNotifier eventNotifier = EventNotifier.EventNotifierBuilder.newBuilder()
                .withTenantId(tenantId)
                .withResourceType(resourceType)
                .withHostName(hostName)
                .withEventType(eventType)
                .withAuthToken(authToken)
                .withKafkaUrl(kafkaUrl)
                .withKafkaPublisherId(kafkaPublisherId)
                .withKafkaEventTopic(kafkaEventTopic).build();

        SaturationGauge saturationGauge = new SaturationGauge();
        saturationGauge.start(eventNotifier);

        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path base = Paths.get(basePath);
        watchKeyPathMap.put(base.register(watchService, StandardWatchEventKinds.ENTRY_CREATE), base);

        registerPathsRecursively(base, 0, watchService);

        boolean poll = true;

        while (poll) {
            WatchKey key = watchService.take();
            Path parentPath = watchKeyPathMap.get(key);
            for (WatchEvent<?> event : key.pollEvents()) {

                Path eventPath = parentPath.resolve((Path) event.context());
                int eventDepth = base.relativize(eventPath).getNameCount();

                logger.info("Event path " + eventPath.toAbsolutePath().toString() + ", Depth " + eventDepth);

                if (eventDepth <= monitorDepth) {
                    logger.info("Registering new path {}", eventPath.toAbsolutePath().toString());
                    WatchKey newKey = eventPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                    watchKeyPathMap.put(newKey, eventPath);
                }

                if (eventDepth == monitorDepth + 1 && eventPath.toAbsolutePath().toFile().isDirectory()) {
                    logger.info("Detected a candidate directory {}", eventPath.toAbsolutePath().toString());
                    saturationGauge.monitorSaturation(eventPath.toAbsolutePath().toFile());
                }
                //File file = path.resolve((Path) event.context()).toFile();
                //logger.info("Event : " + event.kind() + " Type : " + (file.isDirectory()? "Directory": "File") +  " Name : " + event.context());
                //if (file.isDirectory()) {
                    //saturationGauge.monitorSaturation(file);
                //}
            }
            poll = key.reset();
        }
    }
}
