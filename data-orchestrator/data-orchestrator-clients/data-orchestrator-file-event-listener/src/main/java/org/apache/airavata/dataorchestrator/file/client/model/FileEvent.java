package org.apache.airavata.dataorchestrator.file.client.model;

import org.apache.airavata.dataorchestrator.clients.core.model.NotificationEvent;

import java.io.File;

/**
 * A class representing a file
 */
public class FileEvent extends NotificationEvent {
    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    public FileEvent(File file) {
        super(file);
    }

    public File getFile() {
        return (File) getSource();
    }
}
