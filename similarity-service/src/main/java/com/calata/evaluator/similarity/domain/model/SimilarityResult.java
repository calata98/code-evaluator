package com.calata.evaluator.similarity.domain.model;

import java.time.Instant;

public record SimilarityResult(
        String submissionId,
        String userId,
        String language,
        String code,
        SimilarityTypeDomain type,
        double score,
        String matchedSubmissionId,
        Instant createdAt
) { }
