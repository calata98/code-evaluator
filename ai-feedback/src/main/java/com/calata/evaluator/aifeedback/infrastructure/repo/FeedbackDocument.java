package com.calata.evaluator.aifeedback.infrastructure.repo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("feedbacks")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FeedbackDocument {
    @Id
    public String id;
    @Indexed
    public String evaluationId;
    private String title;
    private String message;
    private String type;
    private String severity;
    private String suggestion;
    private String reference;
    private Instant createdAt;
}
