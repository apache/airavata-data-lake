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
import java.util.Set;

@Entity
@Table(name = "DATA_PARSER_ENTITY")
public class DataParserEntity {

    @Id
    @Column(name = "PARSER_ID")
    private String parserId;

    @Column(name = "NAME")
    private String parserName;

    @Column(name = "IMAGE")
    private String dockerImage;

    @Column(name = "EXEC_COMMAND")
    private String execCommand;

    @Column(name = "INPUT_PATH")
    private String inputPath;

    @Column(name = "OUTPUT_PATH")
    private String outputPath;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "parserEntity", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<DataParserInputInterfaceEntity> inputInterfacesList;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "parserEntity", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<DataParserOutputInterfaceEntity> outputInterfacesList;

    public String getParserId() {
        return parserId;
    }

    public void setParserId(String parserId) {
        this.parserId = parserId;
    }

    public String getParserName() {
        return parserName;
    }

    public void setParserName(String parserName) {
        this.parserName = parserName;
    }

    public String getDockerImage() {
        return dockerImage;
    }

    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public String getExecCommand() {
        return execCommand;
    }

    public void setExecCommand(String execCommand) {
        this.execCommand = execCommand;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public Set<DataParserInputInterfaceEntity> getInputInterfacesList() {
        return inputInterfacesList;
    }

    public void setInputInterfacesList(Set<DataParserInputInterfaceEntity> inputInterfacesList) {
        this.inputInterfacesList = inputInterfacesList;
    }

    public Set<DataParserOutputInterfaceEntity> getOutputInterfacesList() {
        return outputInterfacesList;
    }

    public void setOutputInterfacesList(Set<DataParserOutputInterfaceEntity> outputInterfacesList) {
        this.outputInterfacesList = outputInterfacesList;
    }
}
