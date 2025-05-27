package com.calata.evaluator.producer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.kafka")
@Getter
@Setter
public class KafkaTopicProperties {
    private String submissionsTopic;

    public String getSubmissionsTopic() {
        return submissionsTopic;
    }

    public void setSubmissionsTopic(String submissionsTopic) {
        this.submissionsTopic = submissionsTopic;
    }
}
