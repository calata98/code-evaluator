package com.calata.evaluator.aifeedback;

import com.calata.evaluator.kafkaconfig.KafkaTopicsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ConfigurationPropertiesScan
@Import(KafkaTopicsConfig.class)
public class AIFeedbackApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIFeedbackApplication.class, args);
    }
}
