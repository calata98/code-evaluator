package com.calata.evaluator.aifeedback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AIFeedbackApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIFeedbackApplication.class, args);
    }
}
