package com.calata.evaluator.contracts.events;

import com.calata.evaluator.contracts.types.SimilarityType;

import java.time.Instant;

public record SimilarityComputed(
        String submissionId,
        String userId,
        String language,
        String code,
        SimilarityType type,
        double score,
        String matchedSubmissionId,
        Instant createdAt
) { }
