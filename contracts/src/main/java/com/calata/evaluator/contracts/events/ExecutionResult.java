package com.calata.evaluator.contracts.events;

public record ExecutionResult(
        String submissionId,
        String stdout,
        String stderr,
        long timeMs,
        long memoryMb,
        String status
) {}
