package org.apache.airavata.drms.api.utils;

import io.grpc.Context;
import org.apache.airavata.datalake.drms.storage.ResourceSearchQuery;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    public static Optional<String> getMetadataSearchQuery(List<ResourceSearchQuery> resourceSearchQueries, String type, String storageId) {
        if (!resourceSearchQueries.isEmpty()) {
            String preRegex = "'(?i).*";
            String postRegex = ".*'";
            String query = "";
            if ((type.equals("FILE") || type.equals("COLLECTION")) && !storageId.isEmpty()) {
                query = " MATCH (s:Storage{entityId:'" + storageId + "'})<-[:CHILD_OF*]-(r:" + type + ")-[:HAS_METADATA*]->(m) WHERE ";
            } else {
                query = " MATCH (r:" + type + ")-[:HAS_METADATA*]->(m) WHERE ";
            }
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

    public static Optional<String> getPropertySearchQuery(List<ResourceSearchQuery> resourceSearchQueries, String type, String storageId) {
        if (!resourceSearchQueries.isEmpty()) {
            for (ResourceSearchQuery qry : resourceSearchQueries) {
                String query = "";
                if ((type.equals("FILE") || type.equals("COLLECTION")) && !storageId.isEmpty()) {
                    query = " MATCH (s:Storage{entityId:'" + storageId + "'})<-[:CHILD_OF*]-(r:" + type +
                            ") where r." + qry.getField() + " contains  '" + qry.getValue() + "' Return r ";
                } else {
                    query = " MATCH (r:" + type +
                            ") where r." + qry.getField() + " contains  '" + qry.getValue() + "' Return r ";
                }
                return Optional.ofNullable(query);
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getSharedByQuery(List<ResourceSearchQuery> resourceSearchQueries,
                                                    String tenantId) {
        if (!resourceSearchQueries.isEmpty()) {
            for (ResourceSearchQuery qry : resourceSearchQueries) {
                if (qry.getField().equals("sharedBy")) {
                    String value = qry.getValue();
                    String query = " Match (m)-[r:SHARED_WITH]->(l) where r.sharedBy=$sharedBy AND m.tenantId=$tenantId and l.tenantId=$tenantId " +
                            "return (m) ";
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("sharedBy", value);
                    objectMap.put("tenantId", tenantId);
                    return Optional.ofNullable(query);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getSharedQueryExceptOwner(List<ResourceSearchQuery> resourceSearchQueries, String type) {
        if (!resourceSearchQueries.isEmpty()) {
            for (ResourceSearchQuery qry : resourceSearchQueries) {
                String query = " MATCH (r:" + type +
                        ") where r." + qry.getField() + " contains  '" + qry.getValue() + "' Return r ";
                return Optional.ofNullable(query);
            }
        }
        return Optional.empty();
    }

    public static String getId(String message) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        byte[] array = md.digest(message.getBytes(StandardCharsets.UTF_8));
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, array);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }


}
