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
package org.apache.airavata.datalake.orchestrator.api.model;

public class TransferEntry {

    private String id;
    private String sourceRemoteId;
    private String sourcePath;

    private String destRemoteId;
    private String destPath;

    public String getId() {
        return id;
    }

    public TransferEntry setId(String id) {
        this.id = id;
        return this;
    }

    public String getSourceRemoteId() {
        return sourceRemoteId;
    }

    public TransferEntry setSourceRemoteId(String sourceRemoteId) {
        this.sourceRemoteId = sourceRemoteId;
        return this;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public TransferEntry setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
        return this;
    }

    public String getDestRemoteId() {
        return destRemoteId;
    }

    public TransferEntry setDestRemoteId(String destRemoteId) {
        this.destRemoteId = destRemoteId;
        return this;
    }

    public String getDestPath() {
        return destPath;
    }

    public TransferEntry setDestPath(String destPath) {
        this.destPath = destPath;
        return this;
    }
}
