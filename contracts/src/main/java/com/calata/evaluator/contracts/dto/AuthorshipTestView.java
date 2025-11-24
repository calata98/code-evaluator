package com.calata.evaluator.contracts.dto;

import java.time.Instant;
import java.util.List;

public record AuthorshipTestView(
        String submissionId,
        String language,
        Instant createdAt,
        Instant expiresAt,
        List<Question> questions
) {
    public record Question(String id, String prompt, List<String> choices) {}
}
