package org.apache.airavata.datalake.orchestrator;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.data.orchestrator.api.stub.parsing.*;
import org.apache.airavata.datalake.orchestrator.registry.persistance.entity.parser.DataParsingJobInputEntity;
import org.dozer.DozerBeanMapper;

import javax.script.*;
import java.io.File;
import java.util.UUID;
import java.util.function.Predicate;

public class OrchClient {
    public static void main(String are[]) throws ScriptException {
        /*DozerBeanMapper mapper = new DozerBeanMapper();

        DataParsingJobInputEntity dpe = new DataParsingJobInputEntity();
        dpe.setId("id");
        dpe.setDataParsingJobId("dpj");
        dpe.setDataParserInputInterfaceId("dpie");
        dpe.setSelectionQuery("query");

        DataParsingJobInput.Builder builder = DataParsingJobInput.newBuilder();

        mapper.map(dpe, builder);
        System.out.println(builder.build());*/
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6566).usePlaintext().build();
        DataParserServiceGrpc.DataParserServiceBlockingStub parserClient = DataParserServiceGrpc.newBlockingStub(channel);

        //ParsingJobListResponse parsingJobListResponse = parserClient.listParsingJobs(ParsingJobListRequest.newBuilder().build());
        //System.out.println(parsingJobListResponse);
        String parserId = UUID.randomUUID().toString();
        String dm3InputId = UUID.randomUUID().toString();
        String jsonOutputId = UUID.randomUUID().toString();
        ParserRegisterResponse parserRegisterResponse = parserClient.registerParser(ParserRegisterRequest.newBuilder()
                .setParser(DataParser.newBuilder()
                        .setParserId(parserId)
                        .setParserName("DM3 Parser")
                        .setDockerImage("dimuthuupe/dm3-parser:1.0")
                        .setExecCommand("python ./parser.py")
                        .setInputPath("/opt/inputs")
                        .setOutputPath("/opt/outputs")
                        .addInputInterfaces(DataParserInputInterface.newBuilder()
                                .setParserId(parserId)
                                .setParserInputInterfaceId(dm3InputId)
                                .setInputType("FILE")
                                .setInputName("input.dm3").build())
                        .addOutputInterfaces(DataParserOutputInterface.newBuilder()
                                .setParserId(parserId)
                                .setParserOutputInterfaceId(jsonOutputId)
                                .setOutputType("FILE")
                                .setOutputName("derrived-metadata.json")
                                .build())
                        .addOutputInterfaces(DataParserOutputInterface.newBuilder()
                                .setParserId(parserId)
                                .setParserOutputInterfaceId(UUID.randomUUID().toString())
                                .setOutputType("FILE")
                                .setOutputName("img.jpeg").build())
                        .build()).build());

        System.out.println(parserRegisterResponse.getParserId());


        ParserListResponse parserListResponse = parserClient.listParsers(ParserListRequest.newBuilder().build());
        System.out.println(parserListResponse);

        String parsingJobId = UUID.randomUUID().toString();
        ParsingJobRegisterRequest.Builder parsingJob = ParsingJobRegisterRequest.newBuilder().setParsingJob(DataParsingJob.newBuilder()
                .setDataParsingJobId(parsingJobId)
                .setParserId(parserId)
                .addDataParsingJobInputs(DataParsingJobInput.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setDataParserInputInterfaceId(dm3InputId)
                        .setDataParsingJobId(parsingJobId)
                        .setSelectionQuery("EXTENSION == dm3").build())
                .addDataParsingJobOutputs(DataParsingJobOutput.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setDataParsingJobId(parsingJobId)
                        .setOutputType("JSON")
                        .setDataParserOutputInterfaceId(jsonOutputId).build()).build());
        ParsingJobRegisterResponse parsingJobRegisterResponse = parserClient.registerParsingJob(parsingJob.build());

        System.out.println(parsingJobRegisterResponse);
        /*File f = new File("/tmp/a.dm3");

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) s -> true);
        bindings.put("inputFile", f);
        System.out.println(engine.eval("metadata.getFriendlyName().endsWith(\".dm3\");")); // it will not work without allowHostAccess and allowHostClassLookup*/
    }
 }
