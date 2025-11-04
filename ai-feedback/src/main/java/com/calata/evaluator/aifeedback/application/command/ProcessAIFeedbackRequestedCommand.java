package com.calata.evaluator.aifeedback.application.command;

public record ProcessAIFeedbackRequestedCommand(
        String evaluationId,
        String submissionId,
        String language,
        String code
) { }
