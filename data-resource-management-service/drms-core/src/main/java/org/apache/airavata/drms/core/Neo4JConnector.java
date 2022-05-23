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
package org.apache.airavata.drms.core;

import org.neo4j.driver.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neo4JConnector {

    private String uri;
    private String userName;
    private String password;

    private Driver driver;

    public Neo4JConnector() {
    }

    public Neo4JConnector(String uri, String userName, String password) {
        this.uri = uri;
        this.userName = userName;
        this.password = password;
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(userName, password));
    }


    public synchronized Session resume() {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(userName, password));
        return driver.session();
    }

    public void init(String uri, String userName, String password) {
        this.uri = uri;
        this.userName = userName;
        this.password = password;
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(userName, password));
    }

    public List<Record> searchNodes(String query) {
        driver.verifyConnectivity();
        try (Session session = driver.session()) {
            Result result = session.run(query);
            return result.list();
        }
    }

    public List<Record> searchNodes(Map<String, Object> properties, String query) {
        driver.verifyConnectivity();
        try (Session session = driver.session()) {
            Result result = session.run(query, properties);
            return result.list();
        }
    }

    public void mergeNode(Map<String, Object> properties, String label, String userId, String entityId,
                          String tenantId) {
        driver.verifyConnectivity();
        try (Session session = driver.session()) {
            Map<String, Object> parameters = new HashMap<>();
            properties.put("entityId", entityId);
            properties.put("tenantId", tenantId);
            parameters.put("props", properties);
            parameters.put("username", userId);
            parameters.put("entityId", entityId);
            parameters.put("tenantId", tenantId);
            Transaction tx = session.beginTransaction();
            tx.run("MATCH (u:User)  where u.username = $username AND  u.tenantId = $tenantId " +
                    " MERGE (n:" + label + " {entityId: $entityId,tenantId: $tenantId}) ON MATCH  SET n += $props ON CREATE SET n += $props" +
                    " MERGE (n)-[r2:SHARED_WITH {permission:'OWNER'}]->(u) return n", parameters);
            tx.commit();
            tx.close();
        }
    }

    public void mergeNodesWithParentChildRelationShip(Map<String, Object> childProperties, Map<String, Object> parentProperties,
                                                      String childLabel, String parentLablel, String userId, String childEntityId,
                                                      String parentEntityId,
                                                      String tenantId) {
        driver.verifyConnectivity();
        try (Session session = driver.session()) {
            Map<String, Object> parameters = new HashMap<>();
            childProperties.put("childEntityId", childEntityId);
            childProperties.put("tenantId", tenantId);
            parentProperties.put("parentEntityId", parentEntityId);
            parentProperties.put("tenantId", tenantId);
            parameters.put("childProps", childProperties);
            parameters.put("parentProps", parentProperties);
            parameters.put("username", userId);
            parameters.put("childEntityId", childEntityId);
            parameters.put("parentEntityId", parentEntityId);
            parameters.put("tenantId", tenantId);
            Transaction tx = session.beginTransaction();
            tx.run("MATCH (u:User)  where u.username = $username AND  u.tenantId = $tenantId " +
                    " MERGE (p:" + parentLablel + " {entityId: $parentEntityId,tenantId: $tenantId}) ON MATCH  SET p += $parentProps ON CREATE SET p += $parentProps" +
                    " MERGE (c:" + childLabel + " {entityId: $childEntityId,tenantId: $tenantId}) ON MATCH  SET c += $childProps ON CREATE SET c += $childProps" +
                    " MERGE (c)-[:SHARED_WITH {permission:'OWNER'}]->(u)" +
                    " MERGE (p)-[:SHARED_WITH {permission:'OWNER'}]->(u)" +
                    " MERGE (c)-[:CHILD_OF]->(p) return c", parameters);
            tx.commit();
            tx.close();
        }
    }


    public void deleteNode(String label, String entityId,
                           String tenantId) {
        driver.verifyConnectivity();
        try (Session session = driver.session()) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("entityId", entityId);
            parameters.put("tenantId", tenantId);
            Transaction tx = session.beginTransaction();
            tx.run("MATCH (n:" + label + ") where n.entityId= $entityId AND n.tenantId= $tenantId detach delete n", parameters);
            tx.commit();
            tx.close();
        }
    }

    public void runTransactionalQuery(Map<String, Object> parameters, String query) {
        driver.verifyConnectivity();
        try (Session session = driver.session()) {
            Transaction tx = session.beginTransaction();
            Result result = tx.run(query, parameters);
            tx.commit();
            tx.close();
        }
    }


    public void runTransactionalQuery(String query) {
        driver.verifyConnectivity();
        try (Session session = driver.session()) {
            Transaction tx = session.beginTransaction();
            Result result = tx.run(query);
            tx.commit();
            tx.close();
        }
    }

    public void createMetadataNode(String parentLabel, String parentIdName, String parentIdValue,
                                   String userId, String key, String value) {
        driver.verifyConnectivity();
        try(Session session = driver.session()) {
            Transaction tx = session.beginTransaction();
            tx.run("match (u:User)-[r1:MEMBER_OF]->(g:Group)<-[r2:SHARED_WITH]-(s:" + parentLabel + ") where u.userId='" + userId +
                    "' and s." + parentIdName + "='" + parentIdValue +
                    "' merge (m:Metadata)<-[r3:HAS_METADATA]-(s) set m." + key + "='" + value + "' return m");
            tx.commit();
            tx.close();
        }
    }

    public boolean isOpen() {
        driver.verifyConnectivity();
        return driver.session().isOpen();
    }


}
