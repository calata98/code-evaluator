package com.calata.evaluator.similarity.domain.model;

import java.time.Instant;

public record Fingerprint(
        String submissionId,
        String userId,
        String language,
        String shaRaw,
        String shaNorm,
        long simhash64,
        int lineCount,
        Instant createdAt
) { }
