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

package org.apache.airavata.datalake.orchestrator.handlers.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.datalake.data.orchestrator.api.stub.notification.*;
import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.Utils;
import org.apache.airavata.datalake.orchestrator.handlers.async.OrchestratorEventHandler;
import org.apache.airavata.datalake.orchestrator.registry.persistance.entity.notification.NotificationEntity;
import org.apache.airavata.datalake.orchestrator.registry.persistance.entity.notification.NotificationStatusEntity;
import org.apache.airavata.datalake.orchestrator.registry.persistance.repository.NotificationEntityRepository;
import org.apache.airavata.datalake.orchestrator.registry.persistance.repository.NotificationStatusEntityRepository;
import org.dozer.DozerBeanMapper;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@GRpcService
public class NotificationApiHandler extends NotificationServiceGrpc.NotificationServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorEventHandler.class);

    @Autowired
    private NotificationEntityRepository notificationRepository;

    @Autowired
    private NotificationStatusEntityRepository notificationStatusRepository;


    @Autowired
    private OrchestratorEventHandler orchestratorEventHandler;

    @org.springframework.beans.factory.annotation.Value("${config.path}")
    private String configPath;


    @Override
    public void registerNotification(NotificationRegisterRequest request, StreamObserver<NotificationRegisterResponse> responseObserver) {
        DozerBeanMapper mapper = new DozerBeanMapper();
        NotificationEntity notificationEntity = mapper.map(request.getNotification(), NotificationEntity.class);
        notificationRepository.save(notificationEntity);
        responseObserver.onNext(NotificationRegisterResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void listNotifications(NotificationListRequest request, StreamObserver<NotificationListResponse> responseObserver) {
        List<NotificationEntity> allEntities = notificationRepository.findAll();
        DozerBeanMapper mapper = new DozerBeanMapper();
        NotificationListResponse.Builder responseBuilder = NotificationListResponse.newBuilder();
        for (NotificationEntity e : allEntities) {
            Notification.Builder builder = Notification.newBuilder();
            mapper.map(e, builder);
            responseBuilder.addNotifications(builder.build());
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void registerNotificationStatus(NotificationStatusRegisterRequest request, StreamObserver<NotificationStatusRegisterResponse> responseObserver) {
        DozerBeanMapper mapper = new DozerBeanMapper();
        NotificationStatusEntity entity = mapper.map(request.getStatus(), NotificationStatusEntity.class);
        notificationStatusRepository.save(entity);
        responseObserver.onNext(NotificationStatusRegisterResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void listNotificationStatus(NotificationStatusListRequest request, StreamObserver<NotificationStatusListResponse> responseObserver) {
        List<NotificationStatusEntity> allEntities = notificationStatusRepository.findAll();
        DozerBeanMapper mapper = new DozerBeanMapper();
        NotificationStatusListResponse.Builder responseBuilder = NotificationStatusListResponse.newBuilder();
        for (NotificationStatusEntity e : allEntities) {
            NotificationStatus.Builder builder = NotificationStatus.newBuilder();
            mapper.map(e, builder);
            responseBuilder.addStatuses(builder.build());
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void fetchNotificationStatus(NotificationStatusFetchRequest request, StreamObserver<NotificationStatusListResponse> responseObserver) {
        List<NotificationStatusEntity> allEntities = notificationStatusRepository.findByNotificationId(request.getNotificationId());
        DozerBeanMapper mapper = new DozerBeanMapper();
        NotificationStatusListResponse.Builder responseBuilder = NotificationStatusListResponse.newBuilder();
        for (NotificationStatusEntity e : allEntities) {
            NotificationStatus.Builder builder = NotificationStatus.newBuilder();
            mapper.map(e, builder);
            responseBuilder.addStatuses(builder.build());
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }


    @Override
    public void invokeNotification(NotificationInvokeRequest request, StreamObserver<NotificationInvokeResponse> responseObserver) {
        try {
            Optional<NotificationEntity> notificationOP = notificationRepository.findById(request.getNotificationId());

            if (notificationOP.isPresent()) {
                NotificationEntity notificationEntity = notificationOP.get();


                Notification notification = Notification
                        .newBuilder()
                        .setNotificationId(UUID.randomUUID().toString())
                        .setBasePath(notificationEntity.getBasePath())
                        .setResourcePath(notificationEntity.getResourcePath())
                        .setAuthToken(notificationEntity.getAuthToken())
                        .setEventType(Notification.NotificationType.valueOf(notificationEntity.getResourceType()))
                        .setHostName(notificationEntity.getHostName())
                        .setOccuredTime(notificationEntity.getOccuredTime())
                        .setTenantId(notificationEntity.getTenantId())
                        .setResourceType(notificationEntity.getResourceType()).build();

                Configuration configuration = Utils.loadConfig(configPath);
                orchestratorEventHandler.init(configuration);
                orchestratorEventHandler.invokeMessageFlowForNotification(notification);
                responseObserver.onNext(NotificationInvokeResponse.newBuilder().setStatus(true).build());
                responseObserver.onCompleted();
            }
        } catch (Exception ex) {
            String msg = "Notification invocation failed for id " + request.getNotificationId();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }

    }
}
