package com.calata.evaluator.contracts.events;

import java.time.Instant;

public record AuthorshipEvaluationComputed(
        String submissionId,
        String userId,
        String language,
        double confidence,
        String verdict,
        String justification,
        Instant createdAt
) { }
