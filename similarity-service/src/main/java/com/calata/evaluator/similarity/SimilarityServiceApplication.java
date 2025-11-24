package com.calata.evaluator.similarity;

import com.calata.evaluator.kafkaconfig.KafkaTopicsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ConfigurationPropertiesScan
@Import(KafkaTopicsConfig.class)
public class SimilarityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimilarityServiceApplication.class, args);
    }
}
