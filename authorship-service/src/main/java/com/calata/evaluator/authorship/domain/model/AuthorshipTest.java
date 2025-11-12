package com.calata.evaluator.authorship.domain.model;

import java.time.Instant;
import java.util.List;

public record AuthorshipTest(
        String testId,
        String submissionId,
        String userId,
        String language,
        List<AuthorshipQuestion> questions,
        Instant createdAt,
        Instant expiresAt,
        boolean answered
) {}
