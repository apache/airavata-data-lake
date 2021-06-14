package org.apache.airavata.dataorchestrator.file.client.Listener;

import org.apache.airavata.dataorchestrator.clients.core.AbstractListener;
import org.apache.airavata.dataorchestrator.clients.core.EventPublisher;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;

public class FileListener extends AbstractListener {


    public FileListener(EventPublisher eventPublisher) {
        super(eventPublisher);
    }

    @Override
    public void onRegistered(NotificationEvent event) throws Exception {
        super.onRegistered(event);
    }

    @Override
    public void onCreated(NotificationEvent event) throws Exception {
        super.onCreated(event);
    }

    @Override
    public void onModified(NotificationEvent event) throws Exception {
        super.onModified(event);
    }

    @Override
    public void onDeleted(NotificationEvent event) throws Exception {
        super.onDeleted(event);
    }

}
