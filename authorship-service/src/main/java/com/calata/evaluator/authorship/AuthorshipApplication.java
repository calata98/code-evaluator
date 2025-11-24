package com.calata.evaluator.authorship;

import com.calata.evaluator.kafkaconfig.KafkaTopicsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ConfigurationPropertiesScan
@Import(KafkaTopicsConfig.class)
public class AuthorshipApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorshipApplication.class, args);
    }
}
