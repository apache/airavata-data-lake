package org.apache.airavata.datalake.metadata.parsers;

import java.util.HashMap;
import java.util.Map;

public class ExecutionContext {

    private Map<String, Object> neo4JConvertedModels = new HashMap<>();

    public Object getNeo4JConvertedModels(String key) {
        return neo4JConvertedModels.get(key);
    }

    public void addNeo4JConvertedModels(String key, Object obj) {
        this.neo4JConvertedModels.put(key, obj);
    }

    public Map<String, Object> getNeo4JConvertedModels() {
        return neo4JConvertedModels;
    }
}
