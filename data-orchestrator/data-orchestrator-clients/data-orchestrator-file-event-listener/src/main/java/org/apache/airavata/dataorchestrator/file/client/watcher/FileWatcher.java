package org.apache.airavata.dataorchestrator.file.client.watcher;

import org.apache.airavata.dataorchestrator.clients.core.listener.AbstractListener;
import org.apache.airavata.dataorchestrator.file.client.model.FileEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Watch for given folder path and notify changes
 */
public class FileWatcher implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcher.class);
    protected List<AbstractListener> listeners = new ArrayList<>();

    protected final File folder;

    protected static final List<WatchService> watchServices = new ArrayList<>();


    public FileWatcher(File folder) {

        this.folder = folder;

    }

    public void watch() {

        if (folder.exists()) {
            LOGGER.info("Starting watcher thread ...");
            for (AbstractListener listener : listeners) {
                listener.onRegistered(new FileEvent(folder));
            }

            Thread thread = new Thread(this);

//            thread.setDaemon(true);

            thread.start();

        }
    }

    @Override

    public void run() {

        LOGGER.info("FileWatcher started at " + folder.getAbsolutePath());
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            Path path = Paths.get(folder.getAbsolutePath());

            path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            watchServices.add(watchService);

            boolean poll = true;
            while (poll) {

                poll = pollEvents(watchService);

            }

        } catch (IOException | InterruptedException | ClosedWatchServiceException e) {
            LOGGER.error("Error occurred while watching  folders ", e);
            Thread.currentThread().interrupt();

        }

    }


    protected boolean pollEvents(WatchService watchService) throws InterruptedException {

        WatchKey key = watchService.take();

        Path path = (Path) key.watchable();

        for (WatchEvent<?> event : key.pollEvents()) {

            notifyListeners(event.kind(), path.resolve((Path) event.context()).toFile());

        }

        return key.reset();

    }


    protected void notifyListeners(WatchEvent.Kind<?> kind, File file) {

        FileEvent event = new FileEvent(file);

        if (kind == ENTRY_CREATE) {

            for (AbstractListener listener : listeners) {

                listener.onCreated(event);

            }

            if (file.isDirectory()) {

                new FileWatcher(file).setListeners(listeners).watch();

            }

        } else if (kind == ENTRY_MODIFY) {

            for (AbstractListener listener : listeners) {

                listener.onModified(event);

            }

        } else if (kind == ENTRY_DELETE) {

            for (AbstractListener listener : listeners) {

                listener.onDeleted(event);

            }

        }

    }


    public FileWatcher addListener(AbstractListener listener) {

        listeners.add(listener);

        return this;

    }


    public FileWatcher removeListener(AbstractListener listener) {

        listeners.remove(listener);

        return this;

    }


    public List<AbstractListener> getListeners() {

        return listeners;

    }


    public FileWatcher setListeners(List<AbstractListener> listeners) {

        this.listeners = listeners;

        return this;

    }


    public static List<WatchService> getWatchServices() {

        return Collections.unmodifiableList(watchServices);

    }


}
