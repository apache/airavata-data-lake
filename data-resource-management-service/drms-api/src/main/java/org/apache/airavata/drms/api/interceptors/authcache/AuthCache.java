package org.apache.airavata.drms.api.interceptors.authcache;

import org.apache.airavata.datalake.drms.AuthenticatedUser;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AuthCache {
    private static volatile ConcurrentHashMap authCache = new ConcurrentHashMap();

    private static final int MAX_NUMBER_OF_ENTRIES = 1000;
    private static final long MAX_CACHE_TIME = 30 * 60 * 1000;


    public static void cache(CacheEntry cacheEntry) {
        if (authCache.size() == MAX_NUMBER_OF_ENTRIES) {
            //TODO replace with FIFO
            authCache.clear();
        }
        authCache.put(cacheEntry.getAccessToken(), cacheEntry);
    }

    public static Optional<AuthenticatedUser> getAuthenticatedUser(String accessToken) {
        if (authCache.containsKey(accessToken)) {
            CacheEntry cacheEntry = (CacheEntry) authCache.get(accessToken);
            long insertionTime = cacheEntry.getInsertionTime();
            long entryExpiredTimeStamp = insertionTime + MAX_CACHE_TIME;
            if (System.currentTimeMillis() - entryExpiredTimeStamp > 0) {
                authCache.remove(accessToken);
                return Optional.empty();
            } else {
                return Optional.ofNullable(((CacheEntry) authCache.get(accessToken)).getUser());
            }
        }
        return Optional.empty();
    }
}
