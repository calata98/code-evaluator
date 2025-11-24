package com.calata.evaluator.evaluation.orchestrator.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.submission-api")
public record SubmissionApiProps(String baseUrl) {}
