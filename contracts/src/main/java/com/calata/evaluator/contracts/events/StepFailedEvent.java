package com.calata.evaluator.contracts.events;

public record StepFailedEvent(
        String submissionId,
        String stepName,
        String errorCode,
        String errorMessage
) {}
