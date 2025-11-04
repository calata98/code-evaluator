package com.calata.evaluator.evaluation.orchestrator.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProps(
        String submissions,
        String executionRequests,
        String executionResults,
        String evaluationCreated,
        String aiFeedbackRequested
) {}
