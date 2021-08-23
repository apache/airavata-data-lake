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

package org.apache.airavata.datalake.orchestrator.registry.persistance.entity.parser;

import javax.persistence.*;

@Entity
@Table(name = "DATA_PARSING_JOB_OUTPUT_ENTITY")
public class DataParsingJobOutputEntity {

    @Id
    @Column(name = "DATA_PARSING_JOB_OUTPUT_ID")
    private String id;

    @Column(name = "DATA_PARSER_OUTPUT_INTERFACE_ID")
    private String dataParserOutputInterfaceId;

    @Column(name = "DATA_PARSING_JOB_ID")
    private String dataParsingJobId;

    @Column(name = "OUTPUT_TYPE")
    private DataParsingJobOutputType outputType;

    @Column(name = "OUTPUT_DIR_RESOURCE_ID")
    private String outputDirectoryResourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATA_PARSER_OUTPUT_INTERFACE_ID", insertable=false, updatable=false)
    private DataParserOutputInterfaceEntity dataParserOutputInterface;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATA_PARSING_JOB_ID", insertable=false, updatable=false)
    private DataParsingJobEntity dataParsingJobEntity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataParserOutputInterfaceId() {
        return dataParserOutputInterfaceId;
    }

    public void setDataParserOutputInterfaceId(String dataParserOutputInterfaceId) {
        this.dataParserOutputInterfaceId = dataParserOutputInterfaceId;
    }

    public String getDataParsingJobId() {
        return dataParsingJobId;
    }

    public void setDataParsingJobId(String dataParsingJobId) {
        this.dataParsingJobId = dataParsingJobId;
    }

    public DataParsingJobOutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(DataParsingJobOutputType outputType) {
        this.outputType = outputType;
    }

    public DataParserOutputInterfaceEntity getDataParserOutputInterface() {
        return dataParserOutputInterface;
    }

    public void setDataParserOutputInterface(DataParserOutputInterfaceEntity dataParserOutputInterface) {
        this.dataParserOutputInterface = dataParserOutputInterface;
    }

    public DataParsingJobEntity getDataParsingJobEntity() {
        return dataParsingJobEntity;
    }

    public void setDataParsingJobEntity(DataParsingJobEntity dataParsingJobEntity) {
        this.dataParsingJobEntity = dataParsingJobEntity;
    }

    public String getOutputDirectoryResourceId() {
        return outputDirectoryResourceId;
    }

    public void setOutputDirectoryResourceId(String outputDirectoryResourceId) {
        this.outputDirectoryResourceId = outputDirectoryResourceId;
    }
}
