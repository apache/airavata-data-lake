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

import org.apache.airavata.datalake.orchestrator.registry.persistance.entity.parser.DataParserEntity;

import javax.persistence.*;

@Entity
@Table(name = "DATA_PARSER_OUTPUT_INTERFACE_ENTITY")
public class DataParserOutputInterfaceEntity {

    @Id
    @Column(name = "OUTPUT_INTERFACE_ID")
    private String parserOutputInterfaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARSER_ID", insertable=false, updatable=false)
    private DataParserEntity parserEntity;

    @Column(name = "OUTPUT_TYPE")
    private String outputType;

    @Column(name = "OUTPUT_NAME")
    private String outputName;

    @Column(name = "PARSER_ID")
    private String parserId;

    public String getParserOutputInterfaceId() {
        return parserOutputInterfaceId;
    }

    public void setParserOutputInterfaceId(String parserOutputInterfaceId) {
        this.parserOutputInterfaceId = parserOutputInterfaceId;
    }

    public DataParserEntity getParserEntity() {
        return parserEntity;
    }

    public void setParserEntity(DataParserEntity parserEntity) {
        this.parserEntity = parserEntity;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public String getParserId() {
        return parserId;
    }

    public void setParserId(String parserId) {
        this.parserId = parserId;
    }
}
