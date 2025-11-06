package com.calata.evaluator.authorship.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProps(
        String similarityComputed,
        String authorshipAnswersProvided,
        String authorshipTestCreated,
        String authorshipResultComputed
) { }
