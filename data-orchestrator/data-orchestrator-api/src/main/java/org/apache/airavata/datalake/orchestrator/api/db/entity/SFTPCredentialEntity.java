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
import java.util.List;

@Entity
public class SFTPCredentialEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "ID")
    private String id;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "EXISTING_KEY_ID")
    private String existingKeyId;

    @Column(name = "PUBLIC_KEY")
    private String publicKey;

    @Column(name = "PRIVATE_KEY")
    private String privateKey;

    @Column(name = "PASSPHRASE")
    private String passphrase;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "AUTH_METHOD")
    private String authMethod;

    @OneToMany(mappedBy = "credential", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SFTPRemoteEntity> sftpRemotes;

    public String getId() {
        return id;
    }

    public SFTPCredentialEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public SFTPCredentialEntity setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getExistingKeyId() {
        return existingKeyId;
    }

    public SFTPCredentialEntity setExistingKeyId(String existingKeyId) {
        this.existingKeyId = existingKeyId;
        return this;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public SFTPCredentialEntity setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public SFTPCredentialEntity setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public SFTPCredentialEntity setPassphrase(String passphrase) {
        this.passphrase = passphrase;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SFTPCredentialEntity setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public SFTPCredentialEntity setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
        return this;
    }

    public List<SFTPRemoteEntity> getSftpRemotes() {
        return sftpRemotes;
    }

    public SFTPCredentialEntity setSftpRemotes(List<SFTPRemoteEntity> sftpRemotes) {
        this.sftpRemotes = sftpRemotes;
        return this;
    }
}
