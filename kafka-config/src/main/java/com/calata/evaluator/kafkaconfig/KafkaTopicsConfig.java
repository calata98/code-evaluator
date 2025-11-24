package com.calata.evaluator.kafkaconfig;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicsConfig {

    @Bean(name = "kafkaTopicsProperties")
    public KafkaTopicsProperties kafkaTopicsProperties() {
        return new KafkaTopicsProperties();
    }

    private NewTopic createTopic(String name) {
        return new NewTopic(name, 1, (short) 1);
    }

    @Bean
    public NewTopic submissions(KafkaTopicsProperties topics) {
        return createTopic(topics.getSubmissions());
    }

    @Bean
    public NewTopic submissionStatus(KafkaTopicsProperties topics) {
        return createTopic(topics.getSubmissionStatus());
    }

    @Bean
    public NewTopic executionRequests(KafkaTopicsProperties topics) {
        return createTopic(topics.getExecutionRequests());
    }

    @Bean
    public NewTopic executionResults(KafkaTopicsProperties topics) {
        return createTopic(topics.getExecutionResults());
    }

    @Bean
    public NewTopic evaluationCreated(KafkaTopicsProperties topics) {
        return createTopic(topics.getEvaluationCreated());
    }

    @Bean
    public NewTopic aiFeedbackRequested(KafkaTopicsProperties topics) {
        return createTopic(topics.getAiFeedbackRequested());
    }

    @Bean
    public NewTopic aiFeedbackCreated(KafkaTopicsProperties topics) {
        return createTopic(topics.getAiFeedbackCreated());
    }

    @Bean
    public NewTopic similarityComputed(KafkaTopicsProperties topics) {
        return createTopic(topics.getSimilarityComputed());
    }

    @Bean
    public NewTopic authorshipAnswersProvided(KafkaTopicsProperties topics) {
        return createTopic(topics.getAuthorshipAnswersProvided());
    }

    @Bean
    public NewTopic authorshipTestCreated(KafkaTopicsProperties topics) {
        return createTopic(topics.getAuthorshipTestCreated());
    }

    @Bean
    public NewTopic authorshipEvaluationComputed(KafkaTopicsProperties topics) {
        return createTopic(topics.getAuthorshipEvaluationComputed());
    }

    @Bean
    public NewTopic stepFailed(KafkaTopicsProperties topics) {
        return createTopic(topics.getStepFailed());
    }
}

