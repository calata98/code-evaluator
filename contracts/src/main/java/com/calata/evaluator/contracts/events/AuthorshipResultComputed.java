package com.calata.evaluator.contracts.events;

import java.time.Instant;

public record AuthorshipResultComputed(
        String submissionId,
        String userId,
        String language,
        double confidence,
        String verdict,
        String justification,
        Instant createdAt
) { }
