package com.calata.evaluator.consumer.listener;

import com.calata.evaluator.consumer.dto.SubmissionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaSubmissionListener {

    @KafkaListener(topics = "submissions", groupId = "submission-group")
    public void listen(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SubmissionRequest submission = mapper.readValue(message, SubmissionRequest.class);
            System.out.printf("💡 Recibido: userId=%s, exerciseId=%s, code=%s%n",
                    submission.getUserId(), submission.getExerciseId(), submission.getCode());
        } catch (Exception e) {
            System.err.println("❌ Error al deserializar mensaje: " + e.getMessage());
        }
    }
}
