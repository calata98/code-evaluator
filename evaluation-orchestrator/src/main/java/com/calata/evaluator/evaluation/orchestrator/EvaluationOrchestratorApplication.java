package com.calata.evaluator.evaluation.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EvaluationOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvaluationOrchestratorApplication.class, args);
    }
}
