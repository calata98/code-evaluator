package com.calata.evaluator.evaluation.orchestrator;

import com.calata.evaluator.kafkaconfig.KafkaTopicsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ConfigurationPropertiesScan
@Import(KafkaTopicsConfig.class)
public class EvaluationOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvaluationOrchestratorApplication.class, args);
    }
}
