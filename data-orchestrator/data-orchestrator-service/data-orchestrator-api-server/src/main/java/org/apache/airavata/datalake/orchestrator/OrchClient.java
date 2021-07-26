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
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();
        DataParserServiceGrpc.DataParserServiceBlockingStub parserClient = DataParserServiceGrpc.newBlockingStub(channel);

        ParsingJobListResponse parsingJobListResponse = parserClient.listParsingJobs(ParsingJobListRequest.newBuilder().build());
        System.out.println(parsingJobListResponse);
        /*String parserId = UUID.randomUUID().toString();
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
                                .setParserInputInterfaceId(UUID.randomUUID().toString())
                                .setInputType("FILE")
                                .setInputName("input.dm3").build())
                        .addOutputInterfaces(DataParserOutputInterface.newBuilder()
                                .setParserId(parserId)
                                .setParserOutputInterfaceId(UUID.randomUUID().toString())
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
                .setParserId("2f48d838-d0e4-4413-8d8e-4579d4a7369d")
                .addDataParsingJobInputs(DataParsingJobInput.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setDataParserInputInterfaceId("dbb221fc-96d1-4aaf-ae85-73d6215a5106")
                        .setDataParsingJobId(parsingJobId)
                        .setSelectionQuery("EXTENSION == dm3").build())
                .addDataParsingJobOutputs(DataParsingJobOutput.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setDataParsingJobId(parsingJobId)
                        .setOutputType("JSON")
                        .setDataParserOutputInterfaceId("d1ded2cc-ffe6-4e47-a5a2-bdb782b62359").build()).build());
        ParsingJobRegisterResponse parsingJobRegisterResponse = parserClient.registerParsingJob(parsingJob.build()); */

        /*File f = new File("/tmp/a.dm3");

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) s -> true);
        bindings.put("inputFile", f);
        System.out.println(engine.eval("inputFile.getName().endsWith(\".dm3\");")); // it will not work without allowHostAccess and allowHostClassLookup*/
    }
 }
