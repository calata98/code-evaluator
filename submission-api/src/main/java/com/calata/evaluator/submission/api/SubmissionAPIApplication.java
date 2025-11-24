package com.calata.evaluator.submission.api;

import com.calata.evaluator.kafkaconfig.KafkaTopicsConfig;
import com.calata.evaluator.submission.api.infrastructure.repo.SpringDataSubmissionRepository;
import com.calata.evaluator.submission.api.infrastructure.repo.SubmissionDetailViewRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackageClasses = {SpringDataSubmissionRepository.class, SubmissionDetailViewRepository.class})
@SpringBootApplication
@Import({ KafkaTopicsConfig.class})
public class SubmissionAPIApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubmissionAPIApplication.class, args);
    }
}
