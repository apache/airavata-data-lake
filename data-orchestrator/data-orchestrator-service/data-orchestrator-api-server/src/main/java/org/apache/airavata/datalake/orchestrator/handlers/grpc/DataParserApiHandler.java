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

package org.apache.airavata.datalake.orchestrator.handlers.grpc;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.data.orchestrator.api.stub.parsing.*;
import org.apache.airavata.datalake.orchestrator.registry.persistance.entity.parser.DataParserEntity;
import org.apache.airavata.datalake.orchestrator.registry.persistance.entity.parser.DataParsingJobEntity;
import org.apache.airavata.datalake.orchestrator.registry.persistance.repository.DataParserEntityRepository;
import org.apache.airavata.datalake.orchestrator.registry.persistance.repository.DataParsingJobEntityRepository;
import org.dozer.DozerBeanMapper;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GRpcService
public class DataParserApiHandler extends DataParserServiceGrpc.DataParserServiceImplBase {

    @Autowired
    private DataParserEntityRepository parserRepo;

    @Autowired
    private DataParsingJobEntityRepository parsingJobRepo;


    @Override
    public void registerParser(ParserRegisterRequest request, StreamObserver<ParserRegisterResponse> responseObserver) {
        DozerBeanMapper mapper = new DozerBeanMapper();
        DataParserEntity entity = mapper.map(request.getParser(), DataParserEntity.class);
        DataParserEntity saved = parserRepo.save(entity);
        responseObserver.onNext(ParserRegisterResponse.newBuilder().setParserId(saved.getParserId()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void listParsers(ParserListRequest request, StreamObserver<ParserListResponse> responseObserver) {
        DozerBeanMapper mapper = new DozerBeanMapper();

        ParserListResponse.Builder response = ParserListResponse.newBuilder();

        List<DataParserEntity> allParsers = parserRepo.findAll();
        allParsers.forEach(dataParserEntity -> {
            DataParser.Builder parserBuilder = DataParser.newBuilder();
            mapper.map(dataParserEntity, parserBuilder);
            dataParserEntity.getInputInterfacesList().forEach(dataParserInputInterfaceEntity -> {
                DataParserInputInterface.Builder inputBuilder = DataParserInputInterface.newBuilder();
                mapper.map(dataParserInputInterfaceEntity, inputBuilder);
                parserBuilder.addInputInterfaces(inputBuilder);
            });

            dataParserEntity.getOutputInterfacesList().forEach(dataParserOutputInterfaceEntity -> {
                DataParserOutputInterface.Builder outputBuilder = DataParserOutputInterface.newBuilder();
                mapper.map(dataParserOutputInterfaceEntity, outputBuilder);
                parserBuilder.addOutputInterfaces(outputBuilder);
            });
            response.addParsers(parserBuilder);
        });

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void registerParsingJob(ParsingJobRegisterRequest request, StreamObserver<ParsingJobRegisterResponse> responseObserver) {
        DozerBeanMapper mapper = new DozerBeanMapper();
        DataParsingJobEntity entity = mapper.map(request.getParsingJob(), DataParsingJobEntity.class);
        DataParsingJobEntity savedEntity = parsingJobRepo.save(entity);
        responseObserver.onNext(ParsingJobRegisterResponse.newBuilder().setParsingJobId(savedEntity.getDataParsingJobId()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void listParsingJobs(ParsingJobListRequest request, StreamObserver<ParsingJobListResponse> responseObserver) {

        DozerBeanMapper mapper = new DozerBeanMapper();

        ParsingJobListResponse.Builder listBuilder = ParsingJobListResponse.newBuilder();

        List<DataParsingJobEntity> allParsingJobEntities = parsingJobRepo.findAll();
        allParsingJobEntities.forEach(dataParsingJobEntity -> {
            DataParsingJob.Builder parsingJobBuilder = DataParsingJob.newBuilder();
            mapper.map(dataParsingJobEntity, parsingJobBuilder);

            dataParsingJobEntity.getDataParsingJobInputsList().forEach(dataParsingJobInputEntity -> {
                DataParsingJobInput.Builder inputBuilder = DataParsingJobInput.newBuilder();
                mapper.map(dataParsingJobInputEntity, inputBuilder);
                parsingJobBuilder.addDataParsingJobInputs(inputBuilder);
            });

            dataParsingJobEntity.getDataParsingJobOutputsList().forEach(dataParsingJobOutputEntity -> {
                DataParsingJobOutput.Builder outputBuilder = DataParsingJobOutput.newBuilder();
                mapper.map(dataParsingJobOutputEntity, outputBuilder);
                parsingJobBuilder.addDataParsingJobOutputs(outputBuilder);
            });

            listBuilder.addParsers(parsingJobBuilder);
        });

        responseObserver.onNext(listBuilder.build());
        responseObserver.onCompleted();
    }
}
