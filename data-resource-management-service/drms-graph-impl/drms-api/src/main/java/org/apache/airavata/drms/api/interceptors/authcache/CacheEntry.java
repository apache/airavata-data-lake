package org.apache.airavata.drms.api.interceptors.authcache;

import org.apache.airavata.datalake.drms.AuthenticatedUser;

public class CacheEntry {
    private String accessToken;
    private long insertionTime;
    private AuthenticatedUser user;

    public CacheEntry(String accessToken, long insertionTime, AuthenticatedUser user) {
        this.accessToken = accessToken;
        this.insertionTime = insertionTime;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getInsertionTime() {
        return insertionTime;
    }

    public void setInsertionTime(long insertionTime) {
        this.insertionTime = insertionTime;
    }

    public AuthenticatedUser getUser() {
        return user;
    }

    public void setUser(AuthenticatedUser user) {
        this.user = user;
    }
}
