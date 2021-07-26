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
@Table(name = "DATA_PARSING_JOB_INPUT_ENTITY")
public class DataParsingJobInputEntity {

    @Id
    @Column(name = "DATA_PARSING_JOB_INPUT_ID")
    private String id;

    @Column(name = "DATA_PARSER_INPUT_INTERFACE_ID")
    private String dataParserInputInterfaceId;

    @Column(name = "DATA_PARSING_JOB_ID")
    private String dataParsingJobId;

    @Column(name = "SELECTION_QUERY")
    private String selectionQuery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATA_PARSER_INPUT_INTERFACE_ID", insertable=false, updatable=false)
    private DataParserInputInterfaceEntity dataParserInputInterface;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATA_PARSING_JOB_ID", insertable=false, updatable=false)
    private DataParsingJobEntity dataParsingJobEntity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataParserInputInterfaceId() {
        return dataParserInputInterfaceId;
    }

    public void setDataParserInputInterfaceId(String dataParserInputInterfaceId) {
        this.dataParserInputInterfaceId = dataParserInputInterfaceId;
    }

    public String getDataParsingJobId() {
        return dataParsingJobId;
    }

    public void setDataParsingJobId(String dataParsingJobId) {
        this.dataParsingJobId = dataParsingJobId;
    }

    public String getSelectionQuery() {
        return selectionQuery;
    }

    public void setSelectionQuery(String selectionQuery) {
        this.selectionQuery = selectionQuery;
    }

    public DataParserInputInterfaceEntity getDataParserInputInterface() {
        return dataParserInputInterface;
    }

    public void setDataParserInputInterface(DataParserInputInterfaceEntity dataParserInputInterface) {
        this.dataParserInputInterface = dataParserInputInterface;
    }

    public DataParsingJobEntity getDataParsingJobEntity() {
        return dataParsingJobEntity;
    }

    public void setDataParsingJobEntity(DataParsingJobEntity dataParsingJobEntity) {
        this.dataParsingJobEntity = dataParsingJobEntity;
    }
}
