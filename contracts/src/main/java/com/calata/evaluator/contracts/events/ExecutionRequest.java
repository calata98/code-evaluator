package com.calata.evaluator.contracts.events;

public record ExecutionRequest(
        String submissionId,
        String language,
        String code,
        ExecutionConstraints constraints
) {}
