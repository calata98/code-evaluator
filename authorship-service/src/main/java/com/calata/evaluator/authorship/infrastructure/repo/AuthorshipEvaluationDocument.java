package com.calata.evaluator.authorship.infrastructure.repo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("authorship_evaluations")
@Data
public class AuthorshipEvaluationDocument {
    @Id
    private String submissionId;
    private String userId;
    private String language;
    private double confidence;
    private String verdict;
    private String justification;
    private Instant createdAt;
}
