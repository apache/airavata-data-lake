package org.apache.airavata.datalake.orchestrator.workflow.engine.task.impl;

import org.apache.airavata.datalake.orchestrator.workflow.engine.task.NonBlockingTask;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.NonBlockingSection;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.NonBlockingTaskDef;
import org.apache.airavata.datalake.orchestrator.workflow.engine.task.annotation.TaskParam;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@NonBlockingTaskDef(name = "DataParsingWorkflowResourceCleanUpTask")
public class DataParsingWorkflowResourceCleanUpTask extends NonBlockingTask {

    private final static Logger logger = LoggerFactory.getLogger(DataParsingWorkflowResourceCleanUpTask.class);


    @TaskParam(name = "downloadPath")
    private final ThreadLocal<String> downloadPath = new ThreadLocal<>();

    @TaskParam(name = "parsingDir")
    private final List<String> parsingDir = new ArrayList<>();


    @NonBlockingSection(sectionIndex = 1)
    public TaskResult section1() {
        try {
            logger.info("Running donwload file cleanup");

            Files.deleteIfExists(Paths.get(downloadPath.get()));


        } catch (Exception ex) {
            String msg = " downloaded file clean up failed Reason: " + ex.getMessage();
            logger.error(msg, ex);
            return new TaskResult(TaskResult.Status.FAILED, msg);
        }
        return new TaskResult(TaskResult.Status.FAILED, " downloaded file clean up failed");
    }

    @NonBlockingSection(sectionIndex = 2)
    public TaskResult section2() {
        try {
            logger.info("Running parsing directory cleanup");
            for (String path : parsingDir) {
                Files.deleteIfExists(Paths.get(path));
            }

        } catch (Exception exception) {
            String msg = "parsing directory folder clean up failed Reason : " + exception.getMessage();
            logger.error(msg, exception);
            return new TaskResult(TaskResult.Status.FAILED, msg);
        }
        return new TaskResult(TaskResult.Status.FAILED, "parsing directory folder clean up failed");
    }


    public String getDownloadPath() {
        return downloadPath.get();
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath.set(downloadPath);
    }

    public List<String> getParsingDir() {
        return parsingDir;
    }

    public void addParsingDir(String parsingDir) {
        this.parsingDir.add(parsingDir);
    }

}
