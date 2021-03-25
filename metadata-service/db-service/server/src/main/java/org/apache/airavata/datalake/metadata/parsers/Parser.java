package org.apache.airavata.datalake.metadata.parsers;

import com.google.protobuf.GeneratedMessageV3;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Entity;
import org.apache.airavata.datalake.metadata.mergers.Merger;

public interface Parser {

    public Entity parse(GeneratedMessageV3 entity, Entity parentEntity, ExecutionContext executionContext, Merger merger);

    public Entity parse(GeneratedMessageV3 entity, ExecutionContext executionContext);

    public Entity parse(GeneratedMessageV3 entity);

    public Entity parseAndMerge(GeneratedMessageV3 entity);
}
