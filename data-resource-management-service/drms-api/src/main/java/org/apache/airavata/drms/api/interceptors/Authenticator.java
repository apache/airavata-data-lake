package org.apache.airavata.drms.api.interceptors;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.grpc.Metadata;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
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
        User user = identityManagementClient.getUser(token.get());
        AuthenticatedUser authenticatedUser = AuthenticatedUser.newBuilder()
                .setUsername(user.getUsername())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setEmailAddress(user.getEmailAddress())
                .setTenantId(user.getClientId())
                .build();
        return (ReqT) setAuthenticatedUser(msg, authenticatedUser);

    }


    private Optional<String> getAccessToken(Object msg) {
        Descriptors.FieldDescriptor fieldDescriptor =
                ((com.google.protobuf.GeneratedMessageV3) msg).getDescriptorForType().findFieldByName("authToken");
        Object value = ((com.google.protobuf.GeneratedMessageV3) msg).getField(fieldDescriptor);
        DRMSServiceAuthToken drmsServiceAuthToken = (DRMSServiceAuthToken) value;
        return Optional.ofNullable(drmsServiceAuthToken.getAccessToken());
    }

    private Object setAuthenticatedUser(Object msg, AuthenticatedUser user) {

        Descriptors.FieldDescriptor fieldDescriptor =
                ((com.google.protobuf.GeneratedMessageV3) msg).getDescriptorForType().findFieldByName("authToken");
        Object value = ((com.google.protobuf.GeneratedMessageV3) msg).getField(fieldDescriptor);
        DRMSServiceAuthToken drmsServiceAuthToken = (DRMSServiceAuthToken) value;
        drmsServiceAuthToken = drmsServiceAuthToken.toBuilder().setAuthenticatedUser(user).build();
        Message.Builder builder = ((GeneratedMessageV3) msg).toBuilder();

        return builder.setField(fieldDescriptor, drmsServiceAuthToken).build();
    }
}
