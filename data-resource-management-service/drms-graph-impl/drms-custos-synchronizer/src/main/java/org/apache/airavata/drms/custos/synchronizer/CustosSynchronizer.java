package org.apache.airavata.drms.custos.synchronizer;

import org.apache.airavata.drms.custos.synchronizer.datafetcher.CustosDataFetchingJob;
import org.apache.airavata.drms.custos.synchronizer.handlers.events.ConsumerCallback;
import org.apache.airavata.drms.custos.synchronizer.handlers.events.CustosEventListener;
import org.apache.airavata.drms.custos.synchronizer.handlers.events.EventDemux;
import org.apache.custos.messaging.service.Message;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustosSynchronizer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustosSynchronizer.class);

    @org.springframework.beans.factory.annotation.Value("${config.path}")
    private String configPath;

    public static void main(String[] args) {
        SpringApplication.run(CustosSynchronizer.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Starting Custos synchronizer ...");

        LOGGER.info("Configuring scheduler using file {}...", configPath);
        Utils.initializeConnectors(Utils.loadConfiguration(configPath));
        configureScheduler(configPath);
        configureEventListener(configPath);
    }


    private void configureScheduler(String configPath) throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        Configuration configuration = Utils.loadConfig(configPath);
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("custosDataFetcher", "synchronizer1")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds((int) configuration.getPollingInterval())
                        .repeatForever())
                .build();

        JobDetail job = JobBuilder.newJob(CustosDataFetchingJob.class)
                .withIdentity("myJob", "group1")
                .usingJobData("configurationPath", configPath)
                .build();
        scheduler.start();
        scheduler.scheduleJob(job, trigger);
    }


    private void configureEventListener(String configPath) {
        Configuration configuration = Utils.loadConfig(configPath);
        CustosEventListener custosEventListener = new CustosEventListener(
                configuration.getCustos().getCustosBrokerURL(),
                configuration.getCustos().getConsumerGroup(),
                configuration.getCustos().getMaxPollRecordsConfig(),
                configuration.getCustos().getTopics());
        custosEventListener.consume(new ConsumerCallback() {
            @Override
            public void process(Message notificationEvent) throws Exception {
                LOGGER.info("Process from kafka message  Id" + notificationEvent.getMessageId());
                EventDemux.delegateEvents(notificationEvent);
            }
        });
    }
}
