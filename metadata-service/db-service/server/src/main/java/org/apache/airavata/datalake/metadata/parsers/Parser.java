package org.apache.airavata.datalake.metadata.parsers;

import com.google.protobuf.GeneratedMessageV3;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Entity;

public interface Parser {

    public Entity parse(GeneratedMessageV3 entity, Entity parentEntity, ExecutionContext executionContext);

    public Entity parse(GeneratedMessageV3 entity, ExecutionContext executionContext);

    public Entity parse(GeneratedMessageV3 entity);
}
