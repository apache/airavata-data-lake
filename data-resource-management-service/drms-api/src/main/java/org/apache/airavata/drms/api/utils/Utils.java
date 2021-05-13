package org.apache.airavata.drms.api.utils;

import io.grpc.Context;

import java.util.concurrent.ConcurrentHashMap;

public class Utils {

    private static ConcurrentHashMap<String, Context.Key<Object>> keyMap = new ConcurrentHashMap<String, Context.Key<Object>>();

    public static final String CONTEXT_HOLDER =  "CONTEXT_HOLDER";

    public static Context.Key<Object> getUserContextKey() {
        if (keyMap.containsKey("AUTHORIZED_USER")) {
            return keyMap.get("AUTHORIZED_USER");
        }
        keyMap.put("AUTHORIZED_USER", Context.key("AUTHORIZED_USER"));
        return keyMap.get("AUTHORIZED_USER");
    }




}
