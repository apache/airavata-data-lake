/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.datalake.orchestrator.connectors;

import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.core.connector.AbstractConnector;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.iam.service.FindUsersResponse;
import org.apache.custos.iam.service.UserRepresentation;
import org.apache.custos.user.management.client.UserManagementClient;

import java.util.Optional;

public class CustosConnector implements AbstractConnector<Configuration> {

    private UserManagementClient umClient = null;

    public CustosConnector(Configuration configuration) throws Exception {
        this.init(configuration);
    }

    @Override
    public void init(Configuration configuration) throws Exception {
        CustosClientProvider clientProvider = new CustosClientProvider.Builder()
                .setServerHost(configuration.getCustosConfigs().getServerHost())
                .setServerPort(configuration.getCustosConfigs().getServerPort())
                .setClientId(configuration.getCustosConfigs().getClientId())
                .setClientSec(configuration.getCustosConfigs().getClientSec()).build();

        this.umClient = clientProvider.getUserManagementClient();
    }

    @Override
    public void close() throws Exception {
        if (isOpen()) {
            umClient.close();
        }
    }

    public Optional<UserRepresentation> findUserByUserName(String userName) {
        FindUsersResponse userResp = umClient.findUser(userName, "", "", "", 0, 1);
        if (userResp.getUsersCount() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(userResp.getUsers(0));
        }
    }

    public Optional<UserRepresentation> findUserByEmail(String email) {
        FindUsersResponse userResp = umClient.findUser("", "", "", email, 0, 1);
        if (userResp.getUsersCount() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(userResp.getUsers(0));
        }
    }

    @Override
    public boolean isOpen() {
        return umClient != null;
    }
}
