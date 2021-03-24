package org.apache.airavata.drms.api;

import org.apache.airavata.drms.core.Neo4JConnector;
import org.springframework.context.annotation.Bean;
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
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @org.springframework.beans.factory.annotation.Value("${neo4j.server.uri}")
    public String neo4jServerUri;

    @org.springframework.beans.factory.annotation.Value("${neo4j.server.user}")
    public String neo4jServerUser;

    @org.springframework.beans.factory.annotation.Value("${neo4j.server.password}")
    public String neo4jServerPassword;

    @Bean
    public Neo4JConnector neo4JConnector() {
        return new Neo4JConnector(neo4jServerUri, neo4jServerUser, neo4jServerPassword);
    }
}
