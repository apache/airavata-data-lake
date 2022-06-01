package org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl;

import org.apache.airavata.datalake.orchestrator.workflow.engine.task.BlockingTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.BlockingTaskDef;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.TaskParam;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

@BlockingTaskDef(name = "DataParsingWorkflowResourceCleanUpTask")
public class DataParsingWorkflowResourceCleanUpTask extends BlockingTask {

    private final static Logger logger = LoggerFactory.getLogger(DataParsingWorkflowResourceCleanUpTask.class);


    @TaskParam(name = "downloadPath")
    private final ThreadLocal<String> downloadPath = new ThreadLocal<>();

    @TaskParam(name = "parsingDir")
    private final ThreadLocal<String> parsingDir = new ThreadLocal<>();


    public String getDownloadPath() {
        return downloadPath.get();
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath.set(downloadPath);
    }

    public String getParsingDir() {
        return parsingDir.get();
    }

    public void setParsingDir(String parsingDir) {
        this.parsingDir.set(parsingDir);
    }

    @Override
    public TaskResult runBlockingCode() throws Exception {
        try {
            logger.info("Running download file cleanup");

            Files.deleteIfExists(Paths.get(downloadPath.get()));

            logger.info("Running parsing directory cleanup");
            if (!parsingDir.get().isEmpty()) {
                Files.deleteIfExists(Paths.get(parsingDir.get()));
            }

            return new TaskResult(TaskResult.Status.COMPLETED, "Completed");

        } catch (Exception ex) {
            String msg = " files clean up failed Reason: " + ex.getMessage();
            logger.error(msg, ex);
            return new TaskResult(TaskResult.Status.FAILED, msg);
        }

    }
}
