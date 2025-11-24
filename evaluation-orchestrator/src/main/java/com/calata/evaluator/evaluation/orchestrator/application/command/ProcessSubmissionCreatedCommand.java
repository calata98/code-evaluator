package com.calata.evaluator.evaluation.orchestrator.application.command;

public record ProcessSubmissionCreatedCommand(
        String submissionId,
        String code,
        String language,
        String userId
) {}
