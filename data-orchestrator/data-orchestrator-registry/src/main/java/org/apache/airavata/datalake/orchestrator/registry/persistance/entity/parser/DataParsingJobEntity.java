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
import java.util.List;

@Entity
@Table(name = "DATA_PARSING_JOB_ENTITY")
public class DataParsingJobEntity {

    @Id
    @Column(name = "DATA_PARSING_JOB_ID")
    private String dataParsingJobId;

    @Column(name = "PARSER_ID")
    private String parserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARSER_ID", insertable=false, updatable=false)
    private DataParserEntity dataParserEntity;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "dataParsingJobEntity", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<DataParsingJobInputEntity> dataParsingJobInputsList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "dataParsingJobEntity", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<DataParsingJobOutputEntity> dataParsingJobOutputsList;

    public String getDataParsingJobId() {
        return dataParsingJobId;
    }

    public void setDataParsingJobId(String dataParsingJobId) {
        this.dataParsingJobId = dataParsingJobId;
    }

    public String getParserId() {
        return parserId;
    }

    public void setParserId(String parserId) {
        this.parserId = parserId;
    }

    public DataParserEntity getDataParserEntity() {
        return dataParserEntity;
    }

    public void setDataParserEntity(DataParserEntity dataParserEntity) {
        this.dataParserEntity = dataParserEntity;
    }

    public List<DataParsingJobInputEntity> getDataParsingJobInputsList() {
        return dataParsingJobInputsList;
    }

    public void setDataParsingJobInputsList(List<DataParsingJobInputEntity> dataParsingJobInputsList) {
        this.dataParsingJobInputsList = dataParsingJobInputsList;
    }

    public List<DataParsingJobOutputEntity> getDataParsingJobOutputsList() {
        return dataParsingJobOutputsList;
    }

    public void setDataParsingJobOutputsList(List<DataParsingJobOutputEntity> dataParsingJobOutputsList) {
        this.dataParsingJobOutputsList = dataParsingJobOutputsList;
    }
}
