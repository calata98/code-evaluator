package com.calata.evaluator.contracts.events;

import java.time.Instant;

public record SubmissionCreated(
    String id,
    String userId,
    String status,
    String title,
    String language,
    String code,
    Instant createdAt
) {}
