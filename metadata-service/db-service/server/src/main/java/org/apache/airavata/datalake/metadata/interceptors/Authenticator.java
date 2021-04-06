package org.apache.airavata.datalake.metadata.interceptors;

import io.grpc.Metadata;
import org.apache.airavata.datalake.metadata.service.*;
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
        if (msg instanceof TenantMetadataAPIRequest) {
            return msg;
        }
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

        if (msg instanceof TenantMetadataAPIRequest) {
            return Optional.of(((TenantMetadataAPIRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof UserMetadataAPIRequest) {
            return Optional.of(((UserMetadataAPIRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof GroupMetadataAPIRequest) {
            return Optional.of(((GroupMetadataAPIRequest) msg).getAuthToken().getAccessToken());
        } else if (msg instanceof ResourceMetadataAPIRequest) {
            return Optional.of(((ResourceMetadataAPIRequest) msg).getAuthToken().getAccessToken());
        }

        return Optional.empty();
    }

    private Object setAuthenticatedUser(Object msg, AuthenticatedUser user) {

        if (msg instanceof TenantMetadataAPIRequest) {
            MetadataServiceAuthToken metadataServiceAuthToken = ((TenantMetadataAPIRequest) msg)
                    .getAuthToken();
            metadataServiceAuthToken = metadataServiceAuthToken
                    .toBuilder()
                    .setAuthenticatedUser(user)
                    .build();

            return ((TenantMetadataAPIRequest) msg)
                    .toBuilder()
                    .setAuthToken(metadataServiceAuthToken).build();

        } else if (msg instanceof UserMetadataAPIRequest) {
            MetadataServiceAuthToken metadataServiceAuthToken = ((UserMetadataAPIRequest) msg)
                    .getAuthToken();
            metadataServiceAuthToken = metadataServiceAuthToken
                    .toBuilder()
                    .setAuthenticatedUser(user)
                    .build();

            return ((UserMetadataAPIRequest) msg)
                    .toBuilder()
                    .setAuthToken(metadataServiceAuthToken).build();

        } else if (msg instanceof GroupMetadataAPIRequest) {
            MetadataServiceAuthToken metadataServiceAuthToken = ((GroupMetadataAPIRequest) msg)
                    .getAuthToken();
            metadataServiceAuthToken = metadataServiceAuthToken
                    .toBuilder()
                    .setAuthenticatedUser(user)
                    .build();

            return ((GroupMetadataAPIRequest) msg)
                    .toBuilder()
                    .setAuthToken(metadataServiceAuthToken).build();

        } else if (msg instanceof ResourceMetadataAPIRequest) {
            MetadataServiceAuthToken metadataServiceAuthToken = ((ResourceMetadataAPIRequest) msg)
                    .getAuthToken();
            metadataServiceAuthToken = metadataServiceAuthToken
                    .toBuilder()
                    .setAuthenticatedUser(user)
                    .build();

            return ((ResourceMetadataAPIRequest) msg)
                    .toBuilder()
                    .setAuthToken(metadataServiceAuthToken).build();

        }

        return msg;

    }
}
