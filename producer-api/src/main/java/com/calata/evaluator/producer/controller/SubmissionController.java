package com.calata.evaluator.producer.controller;

import com.calata.evaluator.contracts.CodeSubmissionMessage;
import com.calata.evaluator.producer.config.KafkaTopicProperties;
import com.calata.evaluator.producer.dto.SubmissionRequest;
import com.calata.evaluator.producer.kafka.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

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
    public ResponseEntity<String> submitCode(@Valid @RequestBody SubmissionRequest submission) {
        try {
            String submissionId = generateSubmissionId();
            CodeSubmissionMessage message = createCodeSubmissionMessage(submissionId, submission);
            String jsonMessage = objectMapper.writeValueAsString(message);
            producerService.sendMessage(topicProperties.getSubmissionsTopic(), submissionId, jsonMessage);
            return ResponseEntity.accepted().body(Map.of("submissionId", submissionId).toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending message");
        }
    }

    private CodeSubmissionMessage createCodeSubmissionMessage(String submissionId, SubmissionRequest submission) {
        return new CodeSubmissionMessage(
                submissionId,
                submission.getLanguage(),
                submission.getCode(),
                LocalDateTime.now()
        );
    }

    private String generateSubmissionId() {
        return UUID.randomUUID().toString();
    }
}
