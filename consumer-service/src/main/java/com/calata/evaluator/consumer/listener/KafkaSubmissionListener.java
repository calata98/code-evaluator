package com.calata.evaluator.consumer.listener;

import com.calata.evaluator.contracts.CodeSubmissionMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaSubmissionListener {

    private final ObjectMapper mapper;

    public KafkaSubmissionListener(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @KafkaListener(topics = "submissions", groupId = "submission-group")
    public void listen(String message) {
        try {
            CodeSubmissionMessage submission = mapper.readValue(message, CodeSubmissionMessage.class);
            System.out.printf("Received: submissionId=%s, language=%s, code=%s%n",
                    submission.getSubmissionId(), submission.getLanguage(), submission.getCode());
        } catch (Exception e) {
            System.err.println("Error deserializing message: " + e.getMessage());
        }
    }
}
