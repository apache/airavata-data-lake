package org.apache.airavata.dataorchestrator.file.client.watcher;

import org.apache.airavata.dataorchestrator.clients.core.AbstractListener;
import org.apache.airavata.dataorchestrator.file.client.Listener.FileListener;
import org.apache.airavata.dataorchestrator.file.client.model.Configuration;
import org.apache.airavata.dataorchestrator.file.client.publisher.FileEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileWatcherExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcherExecutor.class);
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void startRecursiveWatching(Configuration configuration) throws IOException {
        File folder = new File(configuration.getListeningPath());
        if (folder.exists()) {
            FileWatcher watcher = new FileWatcher(folder, configuration);

            AbstractListener listener = new FileListener(new FileEventPublisher(configuration));
            watcher.addListener(listener);
            submit(watcher);
        } else {
            LOGGER.error("File not found " + folder.getAbsolutePath());
        }
    }

    public static void submit(FileWatcher fileWatcher) {
        executorService.submit(fileWatcher);
    }

}
