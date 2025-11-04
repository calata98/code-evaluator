package com.calata.evaluator.contracts.events;

import java.time.Instant;

public record CodeSubmissionMessage (
    String id,
    String userId,
    String language,
    String code,
    Instant createdAt
) {}
