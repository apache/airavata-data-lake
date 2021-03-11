package org.apache.airavata.datalake.metadata.db.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"org.apache.airavata"})
@SpringBootApplication
public class Neo4JServiceInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Neo4JServiceInitializer.class, args);
    }
}
