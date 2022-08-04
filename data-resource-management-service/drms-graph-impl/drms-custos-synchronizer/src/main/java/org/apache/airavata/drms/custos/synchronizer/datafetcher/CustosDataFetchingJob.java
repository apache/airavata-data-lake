package org.apache.airavata.drms.custos.synchronizer.datafetcher;

import org.apache.airavata.drms.custos.synchronizer.Configuration;
import org.apache.airavata.drms.custos.synchronizer.handlers.SharingHandler;
import org.apache.airavata.drms.custos.synchronizer.handlers.UserAndGroupHandler;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.airavata.drms.custos.synchronizer.Utils.loadConfiguration;

/**
 * Custos data fetching job
 */

public class CustosDataFetchingJob implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustosDataFetchingJob.class);


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            LOGGER.debug("Executing CustosDataFetchingJob ....... ");
            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            String path = jobDataMap.getString("configurationPath");
            Configuration configuration = loadConfiguration(path);
            UserAndGroupHandler userAndGroupHandler = new UserAndGroupHandler();
            userAndGroupHandler.mergeUserAndGroups(configuration);
            SharingHandler sharingHandler = new SharingHandler();
            sharingHandler.mergeSharings(configuration);
        } catch (Exception ex) {
            String msg = "Error occurred while executing job" + ex.getMessage();
            LOGGER.error(msg, ex);
        }


    }


}
