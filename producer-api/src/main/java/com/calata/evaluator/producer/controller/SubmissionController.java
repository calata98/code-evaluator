package com.calata.evaluator.producer.controller;

import com.calata.evaluator.producer.config.KafkaTopicProperties;
import com.calata.evaluator.producer.dto.SubmissionRequest;
import com.calata.evaluator.producer.kafka.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/submit")
public class SubmissionController {

    private final KafkaProducerService producerService;
    private final ObjectMapper objectMapper;
    private final KafkaTopicProperties topicProperties;

    public SubmissionController(KafkaProducerService producerService, ObjectMapper objectMapper,
            @Qualifier("kafkaTopicProperties") KafkaTopicProperties topicProperties) {
        this.producerService = producerService;
        this.objectMapper = objectMapper;
        this.topicProperties = topicProperties;
    }

    @PostMapping
    public ResponseEntity<String> submitCode(@RequestBody SubmissionRequest submission) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(submission);
            producerService.sendMessage(topicProperties.getSubmissionsTopic(), jsonMessage);
            return ResponseEntity.ok("Message sent to Kafka");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending message");
        }
    }
}
