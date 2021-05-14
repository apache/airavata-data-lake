package org.apache.airavata.dataorchestrator.file.client;

import org.apache.airavata.dataorchestrator.file.client.adaptor.FileAdaptor;
import org.apache.airavata.dataorchestrator.file.client.watcher.FileWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class FileClientInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileClientInitializer.class);

    public static void main(String[] args) {
        SpringApplication.run(FileClientInitializer.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Initializing File watcher service ...");
        LOGGER.info("Listening to file path " + args[0]);
        String path = args[0];
        File folder = new File(path);
        FileWatcher watcher = new FileWatcher(folder);
        watcher.addListener(new FileAdaptor()).watch();
    }
}
