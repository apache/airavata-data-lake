package org.apache.airavata.drms.api.utils;

import io.grpc.Context;
import org.apache.airavata.datalake.drms.storage.ResourceSearchQuery;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Utils {

    private static ConcurrentHashMap<String, Context.Key<Object>> keyMap = new ConcurrentHashMap<String, Context.Key<Object>>();

    public static final String CONTEXT_HOLDER = "CONTEXT_HOLDER";

    public static Context.Key<Object> getUserContextKey() {
        if (keyMap.containsKey("AUTHORIZED_USER")) {
            return keyMap.get("AUTHORIZED_USER");
        }
        keyMap.put("AUTHORIZED_USER", Context.key("AUTHORIZED_USER"));
        return keyMap.get("AUTHORIZED_USER");
    }


    public static Optional<String> getMetadataSearchQuery(List<ResourceSearchQuery> resourceSearchQueries, String type) {
        if (!resourceSearchQueries.isEmpty()) {
            String preRegex = "'(?i).*";
            String postRegex = ".*'";
            String query = " MATCH (r:" + type + ")-[:HAS_METADATA*]->(m) WHERE ";
            //TODO: works only for one property
            for (ResourceSearchQuery qry : resourceSearchQueries) {
                if (qry.getField().contains(" ")) {
                    query = " MATCH (r:" + type + ")-[:HAS_METADATA*]->(m:METADATA_NODE{`" + qry.getField() + "`: '" + qry.getValue() + "'})" +
                            " Return r ";
                    return Optional.ofNullable(query);
                } else {
                    String finalSearchStr = preRegex + qry.getValue() + postRegex;
                    query = query + " m." + qry.getField() + "=~ " + finalSearchStr + " AND ";
                }
            }
            query = query.substring(0, query.length() - 5);
            query = query + " RETURN r";
            return Optional.ofNullable(query);

        }
        return Optional.empty();
    }

    public static Optional<String> getPropertySearchQuery(List<ResourceSearchQuery> resourceSearchQueries, String type) {
        if (!resourceSearchQueries.isEmpty()) {
            for (ResourceSearchQuery qry : resourceSearchQueries) {
                String query = " MATCH (r:" + type + "{`" + qry.getField() + "`: '" + qry.getValue() + "'})" +
                        " Return r ";
                return Optional.ofNullable(query);
            }
        }
        return Optional.empty();
    }


}
