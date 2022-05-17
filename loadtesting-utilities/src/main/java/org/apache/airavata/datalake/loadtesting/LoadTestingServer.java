package org.apache.airavata.datalake.loadtesting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"org.apache.airavata.datalake"})
@SpringBootApplication
public class LoadTestingServer {

    public static void main(String[] args) {
        SpringApplication.run(LoadTestingServer.class, args);
    }
}
