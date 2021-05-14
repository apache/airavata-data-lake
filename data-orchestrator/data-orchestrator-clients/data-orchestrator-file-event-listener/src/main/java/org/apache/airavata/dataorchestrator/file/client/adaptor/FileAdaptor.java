package org.apache.airavata.dataorchestrator.file.client.adaptor;

import org.apache.airavata.dataorchestrator.clients.core.listener.AbstractListener;
import org.apache.airavata.dataorchestrator.clients.core.model.NotificationEvent;

public class FileAdaptor extends AbstractListener {

    @Override
    public void onRegistered(NotificationEvent event) {
        super.onRegistered(event);
    }

    @Override
    public void onCreated(NotificationEvent event) {
        super.onCreated(event);
    }

    @Override
    public void onModified(NotificationEvent event) {
        super.onModified(event);
    }

    @Override
    public void onDeleted(NotificationEvent event) {
        super.onDeleted(event);
    }
}
