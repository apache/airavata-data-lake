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
    }

    public void init(String uri, String userName, String password) {
        this.uri = uri;
        this.userName = userName;
        this.password = password;
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(userName, password));
    }

    public List<Record> searchNodes(String query) {
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(userName, password));
        Session session = driver.session();
        Result result = session.run(query);
        return result.list();
    }

    public void createNode(Map<String, Object> properties, String label, String userId) {
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(userName, password));
        Session session = driver.session();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("props", properties);
        Transaction tx = session.beginTransaction();
        Result result = tx.run("match (u:User)-[r1:MEMBER_OF {membershipType:'USER_GROUP'}]->(g:Group) where u.userId = '" + userId + "' CREATE (n:" + label + ")-[r2:SHARED_WITH {permission:'WRITE'}]->(g) SET n = $props return n", parameters);
        tx.commit();
        tx.close();
    }

    public void runTransactionalQuery(Map<String, Object> parameters, String query) {
        Session session = driver.session();
        Transaction tx = session.beginTransaction();
        Result result = tx.run(query, parameters);
        tx.commit();
        tx.close();
    }

    public void runTransactionalQuery(String query) {
        Session session = driver.session();
        Transaction tx = session.beginTransaction();
        Result result = tx.run(query);
        tx.commit();
        tx.close();
    }

    public void createMetadataNode(String parentLabel, String parentIdName, String parentIdValue,
                                   String userId, String key, String value) {
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(userName, password));
        Session session = driver.session();
        Transaction tx = session.beginTransaction();
        tx.run("match (u:User)-[r1:MEMBER_OF]->(g:Group)<-[r2:SHARED_WITH]-(s:" + parentLabel + ") where u.userId='" + userId +
                "' and s." + parentIdName + "='" + parentIdValue +
                "' merge (m:Metadata)<-[r3:HAS_METADATA]-(s) set m." + key + "='" + value + "' return m");
        tx.commit();
        tx.close();
    }

    public boolean isOpen() {
        return driver.session().isOpen();
    }


}
