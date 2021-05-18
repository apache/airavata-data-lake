package org.apache.airavata.datalake.orchestrator.db.inmemory;

import org.apache.airavata.datalake.orchestrator.core.adaptors.StorageAdaptor;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default in memory store that stores all transient events
 */
public class DefaultInMemoryStore implements StorageAdaptor<NotificationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInMemoryStore.class);

    private static final ConcurrentHashMap<String, Stack<NotificationEvent>> inmemoryStore = new ConcurrentHashMap();
    private static final ConcurrentLinkedQueue<String> resourceIdQueue = new ConcurrentLinkedQueue<>();


    @Override
    public void save(NotificationEvent object) {
        inmemoryStore.computeIfAbsent(object.getResourceId(), k -> new Stack<NotificationEvent>()).push(object);
        resourceIdQueue.add(object.getResourceId());
    }

    @Override
    public void delete(String id) {
        inmemoryStore.computeIfPresent(id, (k, v) -> {
            v.remove(id);
            return v;
        });
        resourceIdQueue.remove(id);
    }

    @Override
    public NotificationEvent get(String id) {
        return inmemoryStore.get(id).get(0);
    }

    @Override
    public NotificationEvent update(NotificationEvent object) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Update events are not supported in default inmemory store");
    }

    @Override
    public List<NotificationEvent> poll(int numOfEvents) {
        LOGGER.info("Polling events " + numOfEvents);
        List<NotificationEvent> notificationEventList = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(1);
        int iterations = 0;
        while (count.get() <= numOfEvents) {
            String value = resourceIdQueue.poll();
            List<NotificationEvent> finalNotificationEventList = notificationEventList;
            Optional.ofNullable(value).ifPresent(val-> {
                Stack events =  inmemoryStore.remove(val);
                count.set(count.get() + events.size());
                finalNotificationEventList.addAll(events);
            });
            iterations++;
            if (iterations > 100) {
                break;
            }
        }
        LOGGER.info("Notification event list size " + notificationEventList.size());
        return notificationEventList;
    }


}
