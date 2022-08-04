package org.apache.airavata.drms.api.interceptors;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.google.protobuf.Struct;
import io.grpc.Metadata;
import org.apache.airavata.datalake.drms.AuthCredentialType;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.drms.api.interceptors.authcache.AuthCache;
import org.apache.airavata.drms.api.interceptors.authcache.CacheEntry;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.iam.service.UserRepresentation;
import org.apache.custos.identity.management.client.IdentityManagementClient;
import org.apache.custos.identity.service.User;
import org.apache.custos.user.management.client.UserManagementClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@Component
public class Authenticator implements ServiceInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Authenticator.class);

    @Autowired
    private CustosClientProvider custosClientProvider;


    @Value("${custos.authentication.skip}")
    private boolean skipAuthentication;


    @Override
    public <ReqT> ReqT intercept(String method, Metadata headers, ReqT msg) throws IOException {

         if (skipAuthentication) {
             return  msg;
         }
        Optional<AuthenticatedUser> authenticatorOptional = getAuthenticatedUser(msg, headers);
        if (authenticatorOptional.isPresent()) {
            return (ReqT) setAuthenticatedUser(msg, authenticatorOptional.get());
        } else {
            throw new RuntimeException("Unauthenticated user");
        }

    }


    private Object setAuthenticatedUser(Object msg, AuthenticatedUser user) {

        Descriptors.FieldDescriptor fieldDescriptor =
                ((com.google.protobuf.GeneratedMessageV3) msg).getDescriptorForType().findFieldByName("auth_token");
        Object value = ((com.google.protobuf.GeneratedMessageV3) msg).getField(fieldDescriptor);
        DRMSServiceAuthToken drmsServiceAuthToken = (DRMSServiceAuthToken) value;
        drmsServiceAuthToken = drmsServiceAuthToken.toBuilder().setAuthenticatedUser(user).build();
        Message.Builder builder = ((GeneratedMessageV3) msg).toBuilder();

        return builder.setField(fieldDescriptor, drmsServiceAuthToken).build();
    }

    public Optional<String> getTokenFromHeader(Metadata headers) {
        String tokenWithBearer = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
        if (tokenWithBearer == null) {
            tokenWithBearer = headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));
        }
        if (tokenWithBearer == null) {
            return Optional.empty();
        }
        String prefix = "Bearer";
        String token = tokenWithBearer.substring(prefix.length());
        return Optional.ofNullable(token.trim());
    }

    public Optional<AuthenticatedUser> getAuthenticatedUser(Object msg, Metadata headers) throws IOException {
        try (IdentityManagementClient identityManagementClient = custosClientProvider.getIdentityManagementClient()) {

            try (UserManagementClient userManagementClient = custosClientProvider.getUserManagementClient()) {
                Optional<String> tokenHeaders = getTokenFromHeader(headers);
                if (tokenHeaders.isEmpty()) {
                    //Assume java client is used
                    Descriptors.FieldDescriptor fieldDescriptor =
                            ((com.google.protobuf.GeneratedMessageV3) msg).getDescriptorForType().findFieldByName("auth_token");
                    Object value = ((com.google.protobuf.GeneratedMessageV3) msg).getField(fieldDescriptor);
                    DRMSServiceAuthToken drmsServiceAuthToken = (DRMSServiceAuthToken) value;
                    if (drmsServiceAuthToken.getAuthCredentialType().equals(AuthCredentialType.UNKNOWN) ||
                            drmsServiceAuthToken.getAuthCredentialType().equals(AuthCredentialType.USER_CREDENTIAL)) {
                        String accessToken = drmsServiceAuthToken.getAccessToken();
                        Optional<AuthenticatedUser> optionalAuthenticatedUser = AuthCache.getAuthenticatedUser(accessToken);
                        if (optionalAuthenticatedUser.isPresent()) {
                            return Optional.ofNullable(optionalAuthenticatedUser.get());
                        } else {
                            User user = identityManagementClient.getUser(accessToken);
                            AuthenticatedUser authUser = AuthenticatedUser.newBuilder()
                                    .setUsername(user.getUsername())
                                    .setFirstName(user.getFirstName())
                                    .setLastName(user.getLastName())
                                    .setEmailAddress(user.getEmailAddress())
                                    .setTenantId(user.getClientId())
                                    .build();
                            CacheEntry cacheEntry = new CacheEntry(accessToken, System.currentTimeMillis(), authUser);
                            AuthCache.cache(cacheEntry);
                            return Optional.ofNullable(authUser);
                        }
                    } else if (drmsServiceAuthToken.getAuthCredentialType()
                            .equals(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)) {
                        //Agents use service account to get access token
                        String accessToken = drmsServiceAuthToken.getAccessToken();
                        String decoded = new String(Base64.getDecoder().decode(accessToken));
                        String[] array = decoded.split(":");
                        String agentClientId = array[0];
                        String agentClientSec = array[1];
                        String username = drmsServiceAuthToken.getAuthenticatedUser().getUsername();
                        String tenantId = drmsServiceAuthToken.getAuthenticatedUser().getTenantId();
                        Struct struct = identityManagementClient
                                .getAgentToken(tenantId, agentClientId, agentClientSec, "client_credentials", "");
                        if (struct.getFieldsMap().get("access_token").isInitialized()) {
                            UserRepresentation user = userManagementClient.getUser(username, tenantId);
                            return Optional.ofNullable(AuthenticatedUser.newBuilder()
                                    .setUsername(user.getUsername())
                                    .setFirstName(user.getFirstName())
                                    .setLastName(user.getLastName())
                                    .setEmailAddress(user.getEmail())
                                    .setTenantId(tenantId)
                                    .build());
                        }
                    }
                } else {
                    //Assume rest clients always call with user token
                    Optional<AuthenticatedUser> optionalAuthenticatedUser = AuthCache.getAuthenticatedUser(tokenHeaders.get());
                    if (optionalAuthenticatedUser.isPresent()) {
                        return Optional.ofNullable(optionalAuthenticatedUser.get());
                    } else {
                        User user = identityManagementClient.getUser(tokenHeaders.get());
                        AuthenticatedUser authUser = AuthenticatedUser.newBuilder()
                                .setUsername(user.getUsername())
                                .setFirstName(user.getFirstName())
                                .setLastName(user.getLastName())
                                .setEmailAddress(user.getEmailAddress())
                                .setTenantId(user.getClientId())
                                .build();
                        CacheEntry cacheEntry = new CacheEntry(tokenHeaders.get(), System.currentTimeMillis(), authUser);
                        AuthCache.cache(cacheEntry);
                        return Optional.ofNullable(authUser);
                    }
                }
            }
            return Optional.empty();
        }
    }
}
