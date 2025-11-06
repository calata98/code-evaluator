package com.calata.evaluator.contracts.events;

import java.time.Instant;

public record EvaluationCreated(
        String evaluationId,
        String submissionId,
        String code,
        String language,
        boolean passed,
        String userId,
        int score,
        Instant createdAt
) {}
