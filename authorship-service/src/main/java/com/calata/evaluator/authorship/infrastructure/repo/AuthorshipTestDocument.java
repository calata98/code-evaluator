package com.calata.evaluator.authorship.infrastructure.repo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("authorship_tests")
@Data
public class AuthorshipTestDocument {
    @Id
    private String testId;
    private String submissionId;
    private String userId;
    private String language;
    private List<QuestionDoc> questions;
    private Instant createdAt;
    private Instant expiresAt;
    private boolean answered;

    @Data
    public static class QuestionDoc {
        private String id;
        private String prompt;
        private List<String> choices;
        private Integer correctIndexHint;
    }
}
