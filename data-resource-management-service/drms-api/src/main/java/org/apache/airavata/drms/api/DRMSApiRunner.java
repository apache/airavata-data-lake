package org.apache.airavata.drms.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"org.apache.airavata.drms"})
@SpringBootApplication
public class DRMSApiRunner {
    public static void main(String[] args) {
        SpringApplication.run(DRMSApiRunner.class, args);
    }
}
