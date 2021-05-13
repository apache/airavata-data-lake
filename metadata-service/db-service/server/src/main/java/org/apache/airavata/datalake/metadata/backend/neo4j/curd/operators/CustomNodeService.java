package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import java.util.Map;

public interface CustomNodeService {

    Iterable<Map<String,Object>> execute(String query, Map<String, ?> parameterMap);
}
