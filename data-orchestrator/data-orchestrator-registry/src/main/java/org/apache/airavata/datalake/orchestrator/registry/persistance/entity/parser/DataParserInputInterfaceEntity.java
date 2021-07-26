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
@Table(name = "DATA_PARSER_INPUT_INTERFACE_ENTITY")
public class DataParserInputInterfaceEntity {

    @Id
    @Column(name = "INPUT_INTERFACE_ID")
    private String parserInputInterfaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARSER_ID", insertable=false, updatable=false)
    private DataParserEntity parserEntity;

    @Column(name = "INPUT_TYPE")
    private String inputType;

    @Column(name = "INPUT_NAME")
    private String inputName;

    @Column(name = "PARSER_ID")
    private String parserId;

    public String getParserInputInterfaceId() {
        return parserInputInterfaceId;
    }

    public void setParserInputInterfaceId(String parserInputInterfaceId) {
        this.parserInputInterfaceId = parserInputInterfaceId;
    }

    public DataParserEntity getParserEntity() {
        return parserEntity;
    }

    public void setParserEntity(DataParserEntity parserEntity) {
        this.parserEntity = parserEntity;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public String getParserId() {
        return parserId;
    }

    public void setParserId(String parserId) {
        this.parserId = parserId;
    }
}
