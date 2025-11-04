package com.calata.evaluator.evaluation.orchestrator.application.command;

public record ProcessCodeSubmissionCommand(
        String submissionId,
        String code,
        String language,
        String userId
) {}
