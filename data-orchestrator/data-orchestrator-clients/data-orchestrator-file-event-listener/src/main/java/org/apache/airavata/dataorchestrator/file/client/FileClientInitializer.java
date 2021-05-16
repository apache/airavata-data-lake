package org.apache.airavata.dataorchestrator.file.client;

import org.apache.airavata.dataorchestrator.file.client.model.Configuration;
import org.apache.airavata.dataorchestrator.file.client.watcher.FileWatcherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

@SpringBootApplication
public class FileClientInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileClientInitializer.class);

    public static void main(String[] args) {
        SpringApplication.run(FileClientInitializer.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Initializing File watcher service ...");
        String filePath = null;
        if (args.length > 0) {
            filePath = args[0];
        }

        Configuration configuration = Optional.ofNullable(filePath).
                map(this::loadConfig)
                .orElseGet(() -> this.loadConfig("/Users/isururanawaka/Documents/Airavata_Repository/airavata-data-lake/data-orchestrator/data-orchestrator-clients/data-orchestrator-file-event-listener/src/main/resources/config.yml"));
        FileWatcherExecutor.startRecursiveWatching(configuration);
    }


    private Configuration loadConfig(String filePath) {
        LOGGER.info("File path " + filePath);
        try (InputStream in = new FileInputStream(filePath)) {
            Yaml yaml = new Yaml();
            return yaml.loadAs(in, Configuration.class);
        } catch (Exception exception) {
            LOGGER.error("Error loading config file", exception);
        }
        return null;
    }
}
