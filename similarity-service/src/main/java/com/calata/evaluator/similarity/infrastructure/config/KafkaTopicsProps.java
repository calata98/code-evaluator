package com.calata.evaluator.similarity.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProps(
    String evaluationCreated,
    String similarityComputed
){}
