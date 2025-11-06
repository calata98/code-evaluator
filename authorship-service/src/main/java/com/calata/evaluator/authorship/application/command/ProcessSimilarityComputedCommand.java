package com.calata.evaluator.authorship.application.command;

import java.time.Instant;

public record ProcessSimilarityComputedCommand(
        String submissionId,
        String language,
        String type,
        double score,
        String matchedSubmissionId,
        Instant createdAt,
        String code
) { }
