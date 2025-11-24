package com.calata.evaluator.contracts.dto;

import java.time.Instant;

public record AuthorshipEvaluationView(
        double confidence,
        String verdict,
        String justification,
        Instant createdAt
) {}
