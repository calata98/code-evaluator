package com.calata.evaluator.submission.api.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.kafka.topics")
public class KafkaTopicsProps {
    private String submissions;
    private String submissionStatus;
    private String executionRequests;
    private String executionResults;
}
