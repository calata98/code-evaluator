package com.calata.evaluator.contracts.events;

import java.time.Instant;

public record SubmissionStatusUpdated(
        String id,
        String status,
        Instant updatedAt
) {}
