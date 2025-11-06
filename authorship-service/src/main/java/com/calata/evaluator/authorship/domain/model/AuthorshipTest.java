package com.calata.evaluator.authorship.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuthorshipTest(
        String testId,
        String submissionId,
        String language,
        List<AuthorshipQuestion> questions,
        Instant createdAt,
        Instant expiresAt
) {
    public static AuthorshipTest create(String submissionId, String language, List<AuthorshipQuestion> qs, Instant expiresAt) {
        return new AuthorshipTest(UUID.randomUUID().toString(), submissionId, language, qs, Instant.now(), expiresAt);
    }
}
