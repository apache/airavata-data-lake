package org.apache.airavata.drms.api.utils;

import io.grpc.Context;
import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.drms.storage.ResourceSearchQuery;
import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.airavata.drms.core.deserializer.GenericResourceDeserializer;
import org.neo4j.driver.Record;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
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

    public static List<GenericResource> getMetadataSearchQueryForSharedByMe(String type, String key, String value, String storageId,
                                                                            String sharedBy, String tenantId, Neo4JConnector neo4JConnector) throws Exception {
        String preRegex = "'(?i).*";
        String postRegex = ".*'";
        String query = "";

        if ((type.equals("FILE") || type.equals("COLLECTION")) && !storageId.isEmpty()) {
            query = " MATCH (s:Storage{entityId:'" + storageId + "'})<-[:CHILD_OF*]-(r:" + type + ")-[:HAS_METADATA*]->(m)," +
                    " (r)-[rel:SHARED_WITH]->(l)" +
                    " WHERE ";
        } else {
            query = " MATCH (r:" + type + ")-[:HAS_METADATA*]->(m), (r)-[rel:SHARED_WITH]->(l) " +
                    " WHERE ";
        }

        if (key.contains(" ")) {
            query = " MATCH (r:" + type + ")-[:HAS_METADATA*]->(m:METADATA_NODE{`" + key + "`: '" + value + "'}), (r)-[rel:SHARED_WITH]->(l) " +
                    " where r.sharedBy=$sharedBy AND r.tenantId=$tenantId AND  l.tenantId=$tenantId  AND NOT l.username=$sharedBy  " +
                    " Return r ";
        } else {
            String finalSearchStr = preRegex + value + postRegex;
            query = query + " m." + key + "=~ " + finalSearchStr + " AND  r.sharedBy=$sharedBy AND " +
                    "r.tenantId=$tenantId AND  l.tenantId=$tenantId  AND NOT l.username=$sharedBy return r,rel";
        }

        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("sharedBy", sharedBy);
        objectMap.put("tenantId", tenantId);
        List<Record> records = neo4JConnector.searchNodes(objectMap, query);
        List<String> keyList = new ArrayList<>();
        keyList.add("r:rel");
        return GenericResourceDeserializer.deserializeList(records, keyList);

    }


    public static List<GenericResource> getPropertySearchQueryForSharedByMe(String type, String key, String value, String storageId,
                                                                            String sharedBy, String tenantId, Neo4JConnector neo4JConnector) throws Exception {
        String query = " match (m)-[r:SHARED_WITH]-(l) where r.sharedBy=$sharedBy AND m.tenantId=$tenantId AND " +
                " l.tenantId=$tenantId  AND NOT l.username=$sharedBy " +
                " OPTIONAL MATCH (m)<-[:CHILD_OF*]-(x) where x." + key + " contains  '" + value + "'" +
                " OPTIONAL MATCH (m2)-[r2:SHARED_WITH]->(l) where  m2.tenantId=$tenantId AND  m2." + key + " contains  '" + value + "'" +
                " return m2,r2 ,x,r ";

        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("sharedBy", sharedBy);
        objectMap.put("tenantId", tenantId);
        List<Record> records = neo4JConnector.searchNodes(objectMap, query);
        List<String> keyList = new ArrayList<>();
        keyList.add("m2:r2");
        keyList.add("x:r");
        return GenericResourceDeserializer.deserializeList(records, keyList);
    }


    public static List<GenericResource> getMetadataSearchQueryForSharedWithMe(String type, String key, String value, String storageId,
                                                                              String sharedWith, String tenantId, Neo4JConnector neo4JConnector) throws Exception {
        String preRegex = "'(?i).*";
        String postRegex = ".*'";
        String finalSearchStr = preRegex + value + postRegex;
        String query = "";

        if ((type.equals("FILE") || type.equals("COLLECTION")) && !storageId.isEmpty()) {
            query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId " +
                    " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u)  " +
                    " OPTIONAL MATCH (u)<-[pRel:SHARED_WITH]-(p:COLLECTION)<-[:CHILD_OF*] -(x:" + type + ")-[relR:SHARED_WITH]->(u)," +
                    " (s:Storage{entityId:'" + storageId + "'})<-[:CHILD_OF*]-(x)-[:HAS_METADATA*]->(m)" +
                    " where NOT  x.owner  = $sharedWith AND m." + key + "=~ " + finalSearchStr +
                    " OPTIONAL MATCH (g)<-[pxRel:SHARED_WITH]-(pr:COLLECTION)<-[:CHILD_OF*] -(px:" + type + ")-[relR:SHARED_WITH]->(g)," +
                    " (s:Storage{entityId:'" + storageId + "'})<-[:CHILD_OF*]-(px)-[:HAS_METADATA*]->(m)" +
                    " where NOT  px.owner  = $sharedWith AND m." + key + "=~ " + finalSearchStr +
                    " return distinct  p,pRel, px,pxRel";
        } else {
            query = " MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId " +
                    " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u)  " +
                    " OPTIONAL MATCH (u)<-[pRel:SHARED_WITH]-(p:COLLECTION)<-[:CHILD_OF*] -(x:" + type + ")-[relR:SHARED_WITH]->(u), " +
                    " (x)-[:HAS_METADATA*]->(m)" +
                    " where NOT  x.owner  = $sharedWith AND m." + key + "=~ " + finalSearchStr +
                    " OPTIONAL MATCH (g)<-[pxRel:SHARED_WITH]-(pr:COLLECTION)<-[:CHILD_OF*] -(px:" + type + ")-[relR:SHARED_WITH]->(g), " +
                    " (px)-[:HAS_METADATA*]->(m) " +
                    " where NOT  px.owner  = $sharedWith AND m." + key + "=~ " + finalSearchStr +
                    " return distinct  p,pRel, px,pxRel";
        }

        if (key.contains(" ")) {
            query = "MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId " +
                    " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u)  " +
                    " OPTIONAL MATCH (u)<-[pRel:SHARED_WITH]-(p:COLLECTION)<-[:CHILD_OF*] -(x:" + type + ")-[relR:SHARED_WITH]->(u), " +
                    " (x)-[:HAS_METADATA*]->(m:METADATA_NODE{`" + key + "`: '" + value + "'})" +
                    " where NOT  x.owner  = $sharedWith" +
                    " OPTIONAL MATCH (g)<-[pxRel:SHARED_WITH]-(pr:COLLECTION)<-[:CHILD_OF*] -(px:" + value + ")-[relR:SHARED_WITH]->(g)" +
                    " (px)-[:HAS_METADATA*]->(m:METADATA_NODE{`" + key + "`: '" + value + "'})" +
                    " where NOT  px.owner  = $sharedWith" +
                    " return distinct  p,pRel, px,pxRel";
        }
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("sharedWith", sharedWith);
        objectMap.put("username", sharedWith);
        objectMap.put("tenantId", tenantId);
        List<Record> records = neo4JConnector.searchNodes(objectMap, query);
        List<String> keyList = new ArrayList<>();
        keyList.add("p:pRel");
        keyList.add("px:pxRel");
        return GenericResourceDeserializer.deserializeList(records, keyList);
    }


    public static List<GenericResource> getPropertySearchQueryForSharedWithMe(String type, String key, String value, String storageId,
                                                                              String sharedWith, String tenantId, Neo4JConnector neo4JConnector) throws Exception {
        String query = "MATCH (u:User) where u.username = $username AND u.tenantId = $tenantId " +
                " OPTIONAL MATCH (g:Group)<-[:MEMBER_OF]-(u)  " +
                " OPTIONAL MATCH (u)<-[pRel:SHARED_WITH]-(p:COLLECTION)<-[:CHILD_OF*] -(x:" + type + ")" +
                " where NOT  x.owner  = $sharedWith AND x." + key + " contains  '" + value + "'" +
                " OPTIONAL MATCH (g)<-[pxRel:SHARED_WITH]-(pr:COLLECTION)<-[:CHILD_OF*] -(px:" + type + ")" +
                " where NOT  px.owner  = $sharedWith AND px." + key + " contains  '" + value + "'" +
                " return distinct  x,pRel, px,pxRel";

        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("sharedWith", sharedWith);
        objectMap.put("username", sharedWith);
        objectMap.put("tenantId", tenantId);
        List<Record> records = neo4JConnector.searchNodes(objectMap, query);
        List<String> keyList = new ArrayList<>();
        keyList.add("x:pRel");
        keyList.add("px:pxRel");
        return GenericResourceDeserializer.deserializeList(records, keyList);
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
