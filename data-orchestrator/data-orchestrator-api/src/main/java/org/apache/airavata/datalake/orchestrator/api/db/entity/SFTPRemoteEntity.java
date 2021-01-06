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
package org.apache.airavata.datalake.orchestrator.api.db.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class SFTPRemoteEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "HOST")
    private String host;

    @Column(name = "PORT")
    private Integer port;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "CREDENTIAL_ID")
    private SFTPCredentialEntity credential;

    public String getId() {
        return id;
    }

    public SFTPRemoteEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public SFTPRemoteEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getHost() {
        return host;
    }

    public SFTPRemoteEntity setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public SFTPRemoteEntity setPort(Integer port) {
        this.port = port;
        return this;
    }

    public SFTPCredentialEntity getCredential() {
        return credential;
    }

    public SFTPRemoteEntity setCredential(SFTPCredentialEntity credential) {
        this.credential = credential;
        return this;
    }
}
