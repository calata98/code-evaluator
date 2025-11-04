package com.calata.evaluator.contracts.events;

public record EvaluationCreated(
        String evaluationId,
        String submissionId,
        boolean passed,
        int score,
        long timeMs,
        long memoryMb
) {}
