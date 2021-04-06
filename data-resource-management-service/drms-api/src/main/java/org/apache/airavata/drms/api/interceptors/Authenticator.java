package org.apache.airavata.drms.api.interceptors;

import io.grpc.Metadata;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.identity.management.client.IdentityManagementClient;
import org.apache.custos.identity.service.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class Authenticator implements ServiceInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Authenticator.class);

    @Autowired
    private CustosClientProvider custosClientProvider;


    @Override
    public <ReqT> ReqT intercept(String method, Metadata headers, ReqT msg) throws IOException {
        IdentityManagementClient identityManagementClient = custosClientProvider.getIdentityManagementClient();
        Optional<String> token = getAccessToken(msg);
        LOGGER.info("Token " + token.get());
        User user = identityManagementClient.getUser(token.get());
        AuthenticatedUser authenticatedUser = AuthenticatedUser.newBuilder()
                .setUsername(user.getUsername())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setEmailAddress(user.getEmailAddress())
                .build();
        return (ReqT) setAuthenticatedUser(msg, authenticatedUser);

    }


    private Optional<String> getAccessToken(Object msg) {

        if (msg instanceof StorageCreateRequest) {
            return Optional.of(((StorageCreateRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof StorageFetchRequest) {
            return Optional.of(((StorageFetchRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof StorageUpdateRequest) {
            return Optional.of(((StorageUpdateRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof StorageDeleteRequest) {
            return Optional.of(((StorageDeleteRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof StorageSearchRequest) {
            return Optional.of(((StorageSearchRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof AddStorageMetadataRequest) {
            return Optional.of(((AddStorageMetadataRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof ResourceCreateRequest) {
            return Optional.of(((ResourceCreateRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof ResourceFetchRequest) {
            return Optional.of(((ResourceFetchRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof ResourceUpdateRequest) {
            return Optional.of(((ResourceUpdateRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof ResourceDeleteRequest) {
            return Optional.of(((ResourceDeleteRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof ResourceSearchRequest) {
            return Optional.of(((ResourceSearchRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof AddResourceMetadataRequest) {
            return Optional.of(((AddResourceMetadataRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof FetchResourceMetadataRequest) {
            return Optional.of(((FetchResourceMetadataRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof StoragePreferenceFetchRequest) {
            return Optional.of(((StoragePreferenceFetchRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof StoragePreferenceUpdateRequest) {
            return Optional.of(((StoragePreferenceUpdateRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof StoragePreferenceDeleteRequest) {
            return Optional.of(((StoragePreferenceDeleteRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof StoragePreferenceCreateRequest) {
            return Optional.of(((StoragePreferenceCreateRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof StoragePreferenceSearchRequest) {
            return Optional.of(((StoragePreferenceSearchRequest) msg).getAuthToken().getAccessToken());
        }
        return Optional.empty();
    }

    private Object setAuthenticatedUser(Object msg, AuthenticatedUser user) {

        if (msg instanceof StorageCreateRequest) {

        } else if (msg instanceof StorageFetchRequest) {

        } else if (msg instanceof StorageUpdateRequest) {

        } else if (msg instanceof StorageDeleteRequest) {

        } else if (msg instanceof StorageSearchRequest) {

        } else if (msg instanceof AddStorageMetadataRequest) {

        } else if (msg instanceof ResourceCreateRequest) {

        } else if (msg instanceof ResourceFetchRequest) {

        } else if (msg instanceof ResourceUpdateRequest) {

        } else if (msg instanceof ResourceDeleteRequest) {

        } else if (msg instanceof ResourceSearchRequest) {

        } else if (msg instanceof AddResourceMetadataRequest) {

        } else if (msg instanceof FetchResourceMetadataRequest) {
            DRMSServiceAuthToken drmsServiceAuthToken = ((FetchResourceMetadataRequest) msg)
                    .getAuthToken();
            drmsServiceAuthToken = drmsServiceAuthToken
                    .toBuilder()
                    .setAuthenticatedUser(user)
                    .build();

            return ((FetchResourceMetadataRequest) msg)
                    .toBuilder()
                    .setAuthToken(drmsServiceAuthToken).build();

        } else if (msg instanceof StoragePreferenceFetchRequest) {

        } else if (msg instanceof StoragePreferenceUpdateRequest) {

        } else if (msg instanceof StoragePreferenceDeleteRequest) {

        } else if (msg instanceof StoragePreferenceCreateRequest) {

        } else if (msg instanceof StoragePreferenceSearchRequest) {

        }
        return Optional.empty();
    }
}
