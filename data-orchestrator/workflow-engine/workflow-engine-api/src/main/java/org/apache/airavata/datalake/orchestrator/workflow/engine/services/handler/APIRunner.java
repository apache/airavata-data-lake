package org.apache.airavata.datalake.orchestrator.workflow.engine.services.handler;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(basePackages = {"org.apache.airavata.datalake.orchestrator.workflow.engine.services.handler",
        "org.apache.airavata.datalake.orchestrator.workflow.engine.services.wm"})
@SpringBootApplication()
@EnableJpaAuditing
@EnableJpaRepositories("org.apache.airavata.datalake")
@EntityScan("org.apache.airavata.datalake")
public class APIRunner implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(APIRunner.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("RUnning");
    }

}
