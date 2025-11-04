package com.calata.evaluator.submission.api;

import com.calata.evaluator.submission.api.infrastructure.kafka.KafkaTopicsProps;
import com.calata.evaluator.submission.api.infrastructure.repo.SpringDataSubmissionRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableConfigurationProperties(KafkaTopicsProps.class)
@EnableMongoRepositories(basePackageClasses = SpringDataSubmissionRepository.class)
@SpringBootApplication
public class SubmissionAPIApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubmissionAPIApplication.class, args);
    }
}
