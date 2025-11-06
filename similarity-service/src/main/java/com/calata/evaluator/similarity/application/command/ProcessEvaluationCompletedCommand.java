package com.calata.evaluator.similarity.application.command;

import java.time.Instant;

public record ProcessEvaluationCompletedCommand(
        String submissionId,
        String userId,
        String language,
        String code,
        Instant completedAt
) { }
